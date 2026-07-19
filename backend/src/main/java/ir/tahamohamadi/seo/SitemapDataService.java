package ir.tahamohamadi.seo;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class SitemapDataService {
    private static final int MAX_ENTRIES = 1_000;
    private final JdbcTemplate jdbc;
    public SitemapDataService(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public SitemapDataResponse listPublishedRoutes() {
        List<SitemapEntryResponse> entries = jdbc.query("""
                select locale, canonical_path, last_modified from (
                    select t.language_code as locale,
                           coalesce(nullif(btrim(t.canonical_path), ''), case when p.page_key = 'home' then '/' || t.language_code
                                else '/' || t.language_code || '/pages/' || t.slug end) as canonical_path,
                           greatest(p.updated_at, t.updated_at) as last_modified
                    from content_page_translation t join content_page p on p.id = t.content_page_id
                    where t.deleted_at is null and p.deleted_at is null and p.status = 'PUBLISHED' and p.published_at <= current_timestamp
                    union all
                    select t.language_code, '/' || t.language_code || '/blog/' || t.slug, greatest(p.updated_at, t.updated_at)
                    from blog_post_translation t join blog_post p on p.id = t.blog_post_id
                    where t.deleted_at is null and p.deleted_at is null and p.status = 'PUBLISHED' and p.published_at <= current_timestamp
                    union all
                    select t.language_code, '/' || t.language_code || '/portfolio/' || t.slug, greatest(p.updated_at, t.updated_at)
                    from portfolio_project_translation t join portfolio_project p on p.id = t.portfolio_project_id
                    where t.deleted_at is null and p.deleted_at is null and p.status = 'PUBLISHED'
                    union all
                    select t.language_code, '/' || t.language_code || '/publications/' || t.slug, greatest(p.updated_at, t.updated_at)
                    from publication_translation t join publication p on p.id = t.publication_id
                    where t.deleted_at is null and p.deleted_at is null and p.content_status = 'PUBLISHED'
                ) sitemap_entries order by canonical_path asc, locale asc limit ?
                """, (rs, row) -> new SitemapEntryResponse(rs.getString("locale"), rs.getString("canonical_path"), rs.getTimestamp("last_modified").toInstant()), MAX_ENTRIES);
        return new SitemapDataResponse(entries);
    }
}
