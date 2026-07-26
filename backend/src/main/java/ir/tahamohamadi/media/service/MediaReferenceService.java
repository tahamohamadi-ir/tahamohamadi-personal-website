package ir.tahamohamadi.media.service;

import ir.tahamohamadi.media.api.admin.MediaUsageResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Maintains a normalized, queryable index of media consumers. Source tables remain
 * authoritative; refreshing also backfills records created before V11.
 */
@Service
@ConditionalOnExpression("!'${spring.autoconfigure.exclude:}'.contains('DataSourceAutoConfiguration')")
public class MediaReferenceService {
    private final NamedParameterJdbcTemplate jdbc;

    public MediaReferenceService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Transactional
    public void refreshUsageIndex() {
        jdbc.getJdbcTemplate().update("delete from media_usage");
        jdbc.getJdbcTemplate().update("""
                insert into media_usage (media_asset_id, owner_type, owner_id, reference_key)
                select translation.og_media_id, 'PAGE_OPEN_GRAPH', page.id, 'OPEN_GRAPH'
                from content_page_translation translation join content_page page on page.id = translation.content_page_id
                where translation.og_media_id is not null and translation.deleted_at is null and page.deleted_at is null
                union all select setting.logo_media_id, 'SETTINGS_LOGO', setting.id, 'LOGO'
                from site_setting setting where setting.logo_media_id is not null and setting.deleted_at is null
                union all select setting.og_media_id, 'SETTINGS_OPEN_GRAPH', setting.id, 'OPEN_GRAPH'
                from site_setting setting where setting.og_media_id is not null and setting.deleted_at is null
                union all select (match.captures)[1]::uuid, 'COMPOSER_BLOCK', block.id, 'mediaId'
                from content_page_block block
                cross join lateral regexp_matches(coalesce(block.settings_json, ''), '"mediaId"\\s*:\\s*"([0-9a-fA-F-]{36})"', 'g') as match(captures)
                join media_asset asset on asset.id = (match.captures)[1]::uuid
                where block.deleted_at is null
                union all select post.cover_media_id, 'BLOG_COVER', post.id, 'COVER'
                from blog_post post where post.cover_media_id is not null and post.deleted_at is null
                union all select media.media_asset_id, case when media.usage = 'INLINE' then 'BLOG_INLINE' else 'BLOG_ATTACHMENT' end, media.blog_post_id, media.usage::text
                from blog_post_media media join blog_post post on post.id = media.blog_post_id where post.deleted_at is null
                union all select (match.captures)[1]::uuid, 'BLOG_INLINE', post.id, 'DOCUMENT:' || translation.id
                from blog_post_translation translation join blog_post post on post.id = translation.blog_post_id
                cross join lateral regexp_matches(coalesce(translation.article_document_json, ''), '"mediaId"\\s*:\\s*"([0-9a-fA-F-]{36})"', 'g') as match(captures)
                join media_asset asset on asset.id = (match.captures)[1]::uuid
                where translation.deleted_at is null and post.deleted_at is null
                union all select project.cover_media_id, 'PORTFOLIO_COVER', project.id, 'COVER'
                from portfolio_project project where project.cover_media_id is not null and project.deleted_at is null
                union all select gallery.media_asset_id, 'PORTFOLIO_GALLERY', gallery.portfolio_project_id, gallery.sort_order::text
                from portfolio_project_media gallery join portfolio_project project on project.id = gallery.portfolio_project_id where project.deleted_at is null
                union all select publication.cover_media_id, 'PUBLICATION_COVER', publication.id, 'COVER'
                from publication where publication.cover_media_id is not null and publication.deleted_at is null
                union all select document.media_asset_id, 'RESUME_DOCUMENT', document.id, 'DOCUMENT'
                from resume_document document where document.deleted_at is null
                on conflict do nothing
                """);
    }

    public boolean isReferenced(UUID id) {
        return referencedIds(Set.of(id)).contains(id);
    }

    @Transactional
    public List<MediaUsageResponse> usages(UUID id) {
        refreshUsageIndex();
        return jdbc.query("""
                select usage.owner_type, usage.owner_id,
                    coalesce(page.status, composer_page.status, blog.status, project.status, gallery_project.status, publication.content_status, document.status, 'ACTIVE') as lifecycle_status
                from media_usage usage
                left join content_page page on usage.owner_type = 'PAGE_OPEN_GRAPH' and page.id = usage.owner_id
                left join content_page_block block on usage.owner_type = 'COMPOSER_BLOCK' and block.id = usage.owner_id
                left join content_page composer_page on block.content_page_id = composer_page.id
                left join blog_post blog on usage.owner_type in ('BLOG_COVER', 'BLOG_INLINE', 'BLOG_ATTACHMENT') and blog.id = usage.owner_id
                left join portfolio_project project on usage.owner_type = 'PORTFOLIO_COVER' and project.id = usage.owner_id
                left join portfolio_project gallery_project on usage.owner_type = 'PORTFOLIO_GALLERY' and gallery_project.id = usage.owner_id
                left join publication publication on usage.owner_type = 'PUBLICATION_COVER' and publication.id = usage.owner_id
                left join resume_document document on usage.owner_type = 'RESUME_DOCUMENT' and document.id = usage.owner_id
                where usage.media_asset_id = :id
                order by usage.owner_type, usage.owner_id, usage.reference_key
                """, new MapSqlParameterSource("id", id), (row, ignored) -> new MediaUsageResponse(
                row.getString("owner_type"), row.getObject("owner_id", UUID.class),
                row.getString("lifecycle_status")
        ));
    }

    @Transactional
    public int replaceReferences(UUID sourceId, UUID replacementId) {
        refreshUsageIndex();
        var parameters = new MapSqlParameterSource().addValue("sourceId", sourceId).addValue("replacementId", replacementId);
        int changed = 0;
        changed += jdbc.update("update content_page_translation set og_media_id=:replacementId where og_media_id=:sourceId and deleted_at is null", parameters);
        changed += jdbc.update("update site_setting set logo_media_id=:replacementId where logo_media_id=:sourceId and deleted_at is null", parameters);
        changed += jdbc.update("update site_setting set og_media_id=:replacementId where og_media_id=:sourceId and deleted_at is null", parameters);
        changed += jdbc.update("""
                update content_page_block block set settings_json=replace(block.settings_json, cast(:sourceId as text), cast(:replacementId as text))
                from media_usage usage
                where usage.media_asset_id=:sourceId and usage.owner_type='COMPOSER_BLOCK' and usage.owner_id=block.id
                """, parameters);
        changed += jdbc.update("update blog_post set cover_media_id=:replacementId where cover_media_id=:sourceId and deleted_at is null", parameters);
        changed += jdbc.update("update blog_post_media set media_asset_id=:replacementId where media_asset_id=:sourceId", parameters);
        changed += jdbc.update("update blog_post_translation set article_document_json=replace(article_document_json, cast(:sourceId as text), cast(:replacementId as text)) where article_document_json like '%' || cast(:sourceId as text) || '%' and deleted_at is null", parameters);
        changed += jdbc.update("update portfolio_project set cover_media_id=:replacementId where cover_media_id=:sourceId and deleted_at is null", parameters);
        changed += jdbc.update("update portfolio_project_media set media_asset_id=:replacementId where media_asset_id=:sourceId", parameters);
        changed += jdbc.update("update publication set cover_media_id=:replacementId where cover_media_id=:sourceId and deleted_at is null", parameters);
        changed += jdbc.update("update resume_document set media_asset_id=:replacementId where media_asset_id=:sourceId and deleted_at is null", parameters);
        refreshUsageIndex();
        return changed;
    }

    @Transactional
    public Set<UUID> referencedIds(Collection<UUID> ids) {
        if (ids == null || ids.isEmpty()) return Set.of();
        refreshUsageIndex();
        return Set.copyOf(jdbc.queryForList("select distinct media_asset_id from media_usage where media_asset_id in (:ids)",
                new MapSqlParameterSource("ids", ids), UUID.class));
    }

    @Transactional
    public boolean isPubliclyReferenced(UUID id) {
        refreshUsageIndex();
        Long count = jdbc.queryForObject("""
                select count(*) from media_usage usage
                left join content_page page on usage.owner_type = 'PAGE_OPEN_GRAPH' and page.id = usage.owner_id
                left join content_page_block block on usage.owner_type = 'COMPOSER_BLOCK' and block.id = usage.owner_id
                left join content_page composer_page on block.content_page_id = composer_page.id
                left join blog_post blog on usage.owner_type in ('BLOG_COVER', 'BLOG_INLINE', 'BLOG_ATTACHMENT') and blog.id = usage.owner_id
                left join portfolio_project project on usage.owner_type = 'PORTFOLIO_COVER' and project.id = usage.owner_id
                left join portfolio_project gallery_project on usage.owner_type = 'PORTFOLIO_GALLERY' and gallery_project.id = usage.owner_id
                left join publication publication on usage.owner_type = 'PUBLICATION_COVER' and publication.id = usage.owner_id
                left join resume_document document on usage.owner_type = 'RESUME_DOCUMENT' and document.id = usage.owner_id
                where usage.media_asset_id=:id and (
                    usage.owner_type in ('SETTINGS_LOGO', 'SETTINGS_OPEN_GRAPH')
                    or (usage.owner_type = 'PAGE_OPEN_GRAPH' and page.deleted_at is null and page.status = 'PUBLISHED')
                    or (usage.owner_type = 'COMPOSER_BLOCK' and block.deleted_at is null and block.is_enabled = true and composer_page.deleted_at is null and composer_page.status = 'PUBLISHED')
                    or (usage.owner_type in ('BLOG_COVER', 'BLOG_INLINE', 'BLOG_ATTACHMENT') and blog.deleted_at is null and blog.status = 'PUBLISHED')
                    or (usage.owner_type = 'PORTFOLIO_COVER' and project.deleted_at is null and project.status = 'PUBLISHED')
                    or (usage.owner_type = 'PORTFOLIO_GALLERY' and gallery_project.deleted_at is null and gallery_project.status = 'PUBLISHED')
                    or (usage.owner_type = 'PUBLICATION_COVER' and publication.deleted_at is null and publication.content_status = 'PUBLISHED')
                    or (usage.owner_type = 'RESUME_DOCUMENT' and document.deleted_at is null and document.status = 'PUBLISHED')
                )
                """, Map.of("id", id), Long.class);
        return count != null && count > 0;
    }
}
