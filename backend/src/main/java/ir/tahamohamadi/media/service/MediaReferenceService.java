package ir.tahamohamadi.media.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@ConditionalOnExpression("!'${spring.autoconfigure.exclude:}'.contains('DataSourceAutoConfiguration')")
public class MediaReferenceService {
    private final NamedParameterJdbcTemplate jdbc;

    public MediaReferenceService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public boolean isReferenced(UUID id) {
        return referencedIds(Set.of(id)).contains(id);
    }

    public Set<UUID> referencedIds(Collection<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return Set.of();
        }

        String sql = """
                SELECT DISTINCT media_id
                FROM (
                    SELECT og_media_id AS media_id
                    FROM content_page_translation
                    WHERE og_media_id IS NOT NULL
                    UNION ALL
                    SELECT cover_media_id
                    FROM blog_post
                    WHERE cover_media_id IS NOT NULL
                    UNION ALL
                    SELECT media_asset_id
                    FROM blog_post_media
                    UNION ALL
                    SELECT cover_media_id
                    FROM portfolio_project
                    WHERE cover_media_id IS NOT NULL
                    UNION ALL
                    SELECT cover_media_id
                    FROM publication
                    WHERE cover_media_id IS NOT NULL
                    UNION ALL
                    SELECT media_asset_id
                    FROM resume_document
                ) refs
                WHERE media_id IN (:ids)
                """;

        return Set.copyOf(jdbc.queryForList(
                sql,
                new MapSqlParameterSource("ids", ids),
                UUID.class
        ));
    }

    public boolean isPubliclyReferenced(UUID id) {
        String sql = """
                SELECT count(*)
                FROM (
                    SELECT 1
                    FROM content_page_translation translation
                    JOIN content_page page ON page.id = translation.content_page_id
                    WHERE translation.og_media_id = :id
                      AND translation.deleted_at IS NULL
                      AND page.deleted_at IS NULL
                      AND page.status = 'PUBLISHED'
                    UNION ALL
                    SELECT 1
                    FROM blog_post post
                    WHERE post.cover_media_id = :id
                      AND post.deleted_at IS NULL
                      AND post.status = 'PUBLISHED'
                    UNION ALL
                    SELECT 1
                    FROM blog_post_media media
                    JOIN blog_post post ON post.id = media.blog_post_id
                    WHERE media.media_asset_id = :id
                      AND post.deleted_at IS NULL
                      AND post.status = 'PUBLISHED'
                    UNION ALL
                    SELECT 1
                    FROM portfolio_project project
                    WHERE project.cover_media_id = :id
                      AND project.deleted_at IS NULL
                      AND project.status = 'PUBLISHED'
                    UNION ALL
                    SELECT 1
                    FROM publication publication
                    WHERE publication.cover_media_id = :id
                      AND publication.deleted_at IS NULL
                      AND publication.content_status = 'PUBLISHED'
                    UNION ALL
                    SELECT 1
                    FROM resume_document document
                    WHERE document.media_asset_id = :id
                      AND document.deleted_at IS NULL
                      AND document.status = 'PUBLISHED'
                ) refs
                """;

        Long count = jdbc.queryForObject(sql, Map.of("id", id), Long.class);
        return count != null && count > 0;
    }
}
