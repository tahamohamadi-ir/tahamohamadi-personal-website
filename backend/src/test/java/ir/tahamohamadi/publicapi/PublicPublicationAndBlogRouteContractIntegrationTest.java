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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest(properties = "spring.jpa.hibernate.ddl-auto=validate")
@AutoConfigureMockMvc
class PublicPublicationAndBlogRouteContractIntegrationTest {
    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("publication_blog_contract_test").withUsername("test").withPassword("test");

    private static final Instant PAST = Instant.parse("2026-01-01T00:00:00Z");

    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;

    @BeforeEach
    void setUp() {
        jdbc.execute("TRUNCATE TABLE publication_translation, publication, blog_post_translation, blog_post, blog_category_translation, blog_category CASCADE");
    }

    @Test
    void exposesPublishedPublicationDetailWithOnlyItsEligibleLocalizedAlternates() throws Exception {
        UUID publication = insertPublication("bilingual-paper", "PUBLISHED");
        insertPublicationTranslation(publication, "en", "paper-en");
        insertPublicationTranslation(publication, "fa", "paper-fa");

        mvc.perform(get("/api/v1/public/en/publications/paper-en"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.canonicalPath").value("/en/publications/paper-en"))
                .andExpect(jsonPath("$.availableLocales.length()").value(2))
                .andExpect(jsonPath("$.hreflang[?(@.locale == 'fa')].path").value("/fa/publications/paper-fa"));
    }

    @Test
    void returnsCanonicalTranslationUnavailableErrorWithRealAlternatePathForMissingPublicationLocale() throws Exception {
        UUID publication = insertPublication("english-only-paper", "PUBLISHED");
        insertPublicationTranslation(publication, "en", "paper-en-only");

        mvc.perform(get("/api/v1/public/fa/publications/paper-en-only"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("TRANSLATION_UNAVAILABLE"))
                .andExpect(jsonPath("$.availableLocales").value("en"))
                .andExpect(jsonPath("$.alternatePaths").value("/en/publications/paper-en-only"));
    }

    @Test
    void keepsResourceNotFoundForNonexistentOrInaccessiblePublications() throws Exception {
        UUID draft = insertPublication("draft-paper", "DRAFT");
        insertPublicationTranslation(draft, "en", "draft-paper-en");

        mvc.perform(get("/api/v1/public/en/publications/does-not-exist"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
        mvc.perform(get("/api/v1/public/en/publications/draft-paper-en"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void publishesBlogCanonicalAndHreflangPathsUsingTheBrowserBlogRoute() throws Exception {
        UUID category = insertCategory();
        UUID post = insertPost(category, "blog-en");
        insertPostTranslation(post, "fa", "blog-fa");

        mvc.perform(get("/api/v1/public/en/posts/blog-en"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.canonicalPath").value("/en/blog/blog-en"))
                .andExpect(jsonPath("$.hreflang[?(@.locale == 'fa')].path").value("/fa/blog/blog-fa"));
        mvc.perform(get("/api/v1/public/en/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].canonicalPath").value("/en/blog/blog-en"))
                .andExpect(jsonPath("$.items[0].hreflang[?(@.locale == 'fa')].path").value("/fa/blog/blog-fa"));
        mvc.perform(get("/api/v1/public/sitemap-data"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.items[0].canonicalPath").value("/en/blog/blog-en"))
                .andExpect(jsonPath("$.items[1].canonicalPath").value("/fa/blog/blog-fa"));
        mvc.perform(get("/api/v1/public/sitemap-data"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.items[0].canonicalPath").value("/en/blog/blog-en"))
                .andExpect(jsonPath("$.items[1].canonicalPath").value("/fa/blog/blog-fa"));
    }

    private UUID insertPublication(String key, String status) {
        UUID id = UUID.randomUUID();
        jdbc.update("INSERT INTO publication (id,publication_key,content_status,publication_stage,year,sort_order,created_at,updated_at,version) VALUES (?,?,?,'PUBLISHED',2026,0,?,?,0)", id, key, status, time(PAST), time(PAST));
        return id;
    }

    private void insertPublicationTranslation(UUID publication, String locale, String slug) {
        jdbc.update("INSERT INTO publication_translation (id,publication_id,language_code,title,slug,abstract_text,authors_display,venue_display,seo_title,seo_description,created_at,updated_at,version) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,0)", UUID.randomUUID(), publication, locale, "Title " + locale, slug, "abstract", "authors", "venue", "SEO " + locale, "description", time(PAST), time(PAST));
    }

    private UUID insertCategory() {
        UUID id = UUID.randomUUID();
        jdbc.update("INSERT INTO blog_category (id,category_key,sort_order,is_active,created_at,updated_at,version) VALUES (?,?,0,true,?,?,0)", id, "engineering", time(PAST), time(PAST));
        jdbc.update("INSERT INTO blog_category_translation (id,blog_category_id,language_code,name,slug,created_at,updated_at,version) VALUES (?,?,?,?,?,?,?,0)", UUID.randomUUID(), id, "en", "Engineering", "engineering", time(PAST), time(PAST));
        return id;
    }

    private UUID insertPost(UUID category, String slug) {
        UUID id = UUID.randomUUID();
        jdbc.update("INSERT INTO blog_post (id,category_id,status,published_at,created_at,updated_at,version) VALUES (?,?,'PUBLISHED',?,?,?,0)", id, category, time(PAST), time(PAST), time(PAST));
        insertPostTranslation(id, "en", slug);
        return id;
    }

    private void insertPostTranslation(UUID post, String locale, String slug) {
        jdbc.update("INSERT INTO blog_post_translation (id,blog_post_id,language_code,title,slug,excerpt,body_markdown,seo_title,seo_description,created_at,updated_at,version) VALUES (?,?,?,?,?,?,?,?,?,?,?,0)", UUID.randomUUID(), post, locale, "Post " + locale, slug, "excerpt", "body", "SEO " + locale, "description", time(PAST), time(PAST));
    }

    private static Timestamp time(Instant instant) {
        return Timestamp.from(instant);
    }
}
