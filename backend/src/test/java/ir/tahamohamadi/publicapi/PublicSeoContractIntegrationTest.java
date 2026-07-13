package ir.tahamohamadi.publicapi;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest(properties = "spring.jpa.hibernate.ddl-auto=validate")
@AutoConfigureMockMvc
class PublicSeoContractIntegrationTest {
    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("public_seo_test").withUsername("test").withPassword("test");

    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;

    private final Instant past = Instant.parse("2026-01-01T00:00:00Z");

    @BeforeEach
    void setUp() {
        jdbc.execute("TRUNCATE TABLE media_asset_translation, media_asset, publication_translation, publication, portfolio_project_translation, portfolio_project, blog_post_translation, blog_post, blog_category_translation, blog_category, content_page_translation, content_page CASCADE");
        insertPage("home", "en", "home", "Home", "PUBLISHED", past);
        insertPage("home", "fa", "home-fa", "خانه", "PUBLISHED", past);
        insertPage("about", "en", "about", "About", "PUBLISHED", past);
        insertPage("draft", "en", "draft", "Draft", "DRAFT", past);
        insertPage("future", "en", "future", "Future", "PUBLISHED", Instant.parse("2099-01-01T00:00:00Z"));
        jdbc.update("UPDATE content_page_translation SET canonical_path = ? WHERE language_code = ? AND slug = ?", "/en/custom-home", "en", "home");
        UUID media = UUID.randomUUID();
        jdbc.update("INSERT INTO media_asset (id,storage_key,original_filename,extension,mime_type,size_bytes,checksum_sha256,status,created_at,updated_at,version) VALUES (?,?,?,?,?,?,?,'ACTIVE',?,?,0)", media, "seo/home.png", "home.png", "png", "image/png", 1L, "a".repeat(64), time(past), time(past));
        jdbc.update("INSERT INTO media_asset_translation (id,media_asset_id,language_code,alt_text,created_at,updated_at,version) VALUES (?,?,?,?,?,?,0)", UUID.randomUUID(), media, "en", "Home image", time(past), time(past));
        jdbc.update("UPDATE content_page_translation SET og_media_id = ? WHERE language_code = ? AND slug = ?", media, "en", "home");
    }

    @Test
    void emitsOnlyPublishedLocaleAvailableCanonicalSitemapEntriesInStableOrder() throws Exception {
        mvc.perform(get("/api/v1/public/sitemap-data"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(3))
                .andExpect(jsonPath("$.items[0].canonicalPath").value("/en/custom-home"))
                .andExpect(jsonPath("$.items[1].canonicalPath").value("/en/pages/about"))
                .andExpect(jsonPath("$.items[2].canonicalPath").value("/fa"))
                .andExpect(jsonPath("$..id").doesNotExist())
                .andExpect(jsonPath("$..deletedAt").doesNotExist());
    }

    @Test
    void exposesSeoOpenGraphAndHreflangFromActualEligibleTranslations() throws Exception {
        mvc.perform(get("/api/v1/public/en/pages/home"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.canonicalPath").value("/en/custom-home"))
                .andExpect(jsonPath("$.hreflang.length()").value(2))
                .andExpect(jsonPath("$.hreflang[?(@.locale == 'en')].path").value("/en/custom-home"))
                .andExpect(jsonPath("$.seo.title").value("Home SEO"))
                .andExpect(jsonPath("$.seo.openGraph.title").value("Home SEO"))
                .andExpect(jsonPath("$.seo.openGraph.description").value("description"))
                .andExpect(jsonPath("$.seo.openGraph.imageUrl").value("/api/v1/public/media/" + jdbc.queryForObject("SELECT og_media_id FROM content_page_translation WHERE language_code = 'en' AND slug = 'home'", UUID.class)))
                .andExpect(jsonPath("$.ogMedia.altText").value("Home image"));
        mvc.perform(get("/api/v1/public/en/home"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.canonicalPath").value("/en/custom-home"))
                .andExpect(jsonPath("$.hreflang[?(@.locale == 'en')].path").value("/en/custom-home"));
    }

    @Test
    void treatsBlankCanonicalPathsAsAbsentLikeSitemapAndHreflang() throws Exception {
        jdbc.update("UPDATE content_page_translation SET canonical_path = ? WHERE language_code = ? AND slug = ?", "   ", "en", "about");
        mvc.perform(get("/api/v1/public/en/pages/about"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.canonicalPath").value("/en/pages/about"));
    }

    @Test
    void usesHomeCanonicalPathsForBlankAndMissingLocaleHomeContracts() throws Exception {
        jdbc.update("UPDATE content_page_translation SET canonical_path = ? WHERE language_code = ? AND slug = ?", "   ", "en", "home");
        mvc.perform(get("/api/v1/public/en/home"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.canonicalPath").value("/en"))
                .andExpect(jsonPath("$.hreflang[?(@.locale == 'en')].path").value("/en"));
        mvc.perform(get("/api/v1/public/en/pages/home"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.canonicalPath").value("/en"))
                .andExpect(jsonPath("$.hreflang[?(@.locale == 'en')].path").value("/en"));
        mvc.perform(get("/api/v1/public/sitemap-data"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].canonicalPath").value("/en"));
        jdbc.update("UPDATE content_page_translation SET canonical_path = ? WHERE language_code = ? AND slug = ?", "/en/custom-home", "en", "home");
        jdbc.update("DELETE FROM content_page_translation WHERE language_code = ? AND slug = ?", "fa", "home-fa");
        mvc.perform(get("/api/v1/public/fa/home"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.alternatePaths[0]").value("/en/custom-home"));
    }

    @Test
    void returnsNoIndexRobotsPolicyOutsideProduction() throws Exception {
        mvc.perform(get("/robots.txt"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Robots-Tag", "noindex, nofollow"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().string("User-agent: *\nDisallow: /\n"));
    }

    private void insertPage(String key, String locale, String slug, String title, String status, Instant publishedAt) {
        UUID page = jdbc.query("SELECT id FROM content_page WHERE page_key = ?", (rs, row) -> rs.getObject(1, UUID.class), key).stream().findFirst().orElseGet(() -> {
            UUID id = UUID.randomUUID();
            jdbc.update("INSERT INTO content_page (id,page_key,status,published_at,created_at,updated_at,version) VALUES (?,?,?,?,?,?,0)", id, key, status, time(publishedAt), time(past), time(past));
            return id;
        });
        jdbc.update("INSERT INTO content_page_translation (id,content_page_id,language_code,title,slug,summary,body_markdown,seo_title,seo_description,created_at,updated_at,version) VALUES (?,?,?,?,?,?,?,?,?,?,?,0)", UUID.randomUUID(), page, locale, title, slug, "summary", "body", title + " SEO", "description", time(past), time(past));
    }

    private static Timestamp time(Instant instant) { return Timestamp.from(instant); }
}
