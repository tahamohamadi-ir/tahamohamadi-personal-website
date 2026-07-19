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
import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(properties = "spring.jpa.hibernate.ddl-auto=validate")
@AutoConfigureMockMvc
class PublicBlogSearchIntegrationTest {
    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("public_blog_search_test").withUsername("test").withPassword("test");

    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;

    @BeforeEach
    void setUp() {
        jdbc.execute("TRUNCATE TABLE blog_post_tag, blog_post_translation, blog_post, blog_category_translation, blog_category CASCADE");
        UUID category = UUID.randomUUID();
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        jdbc.update("INSERT INTO blog_category (id,category_key,sort_order,is_active,created_at,updated_at,version) VALUES (?,?,0,true,?,?,0)", category, "engineering", time(now), time(now));
        jdbc.update("INSERT INTO blog_category_translation (id,blog_category_id,language_code,name,slug,created_at,updated_at,version) VALUES (?,?,?,?,?,?,?,0)", UUID.randomUUID(), category, "en", "Engineering", "engineering", time(now), time(now));
        jdbc.update("INSERT INTO blog_category_translation (id,blog_category_id,language_code,name,slug,created_at,updated_at,version) VALUES (?,?,?,?,?,?,?,0)", UUID.randomUUID(), category, "fa", "مهندسی", "engineering-fa", time(now), time(now));
        insertPost(category, "en", "spring-search", "Spring search", "PostgreSQL full text body", now);
        UUID bilingual = insertPost(category, "en", "bilingual-search", "Bilingual search", "Bilingual full text body", now);
        insertPostTranslation(bilingual, "fa", "bilingual-search-fa", "جستجوی دو زبانه", "متن جستجوی دو زبانه", now);
        insertPost(category, "fa", "persian-search", "کتاب", "متن جستجو", now);
    }

    @Test
    void usesBoundedParameterizedFtsWithoutCrossLocaleOrInjectionLeaks() throws Exception {
        mvc.perform(get("/api/v1/public/en/posts").param("q", "PostgreSQL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].slug").value("spring-search"))
                .andExpect(jsonPath("$.items[0].hreflang.length()").value(1))
                .andExpect(jsonPath("$.items[0].hreflang[0].locale").value("en"));
        mvc.perform(get("/api/v1/public/fa/posts").param("q", "كتاب"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].slug").value("persian-search"));
        mvc.perform(get("/api/v1/public/en/posts").param("q", "' OR 1=1 --"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(0));
    }

    @Test
    void rejectsEmptySearchQueriesAndKeepsTheExistingPageBound() throws Exception {
        mvc.perform(get("/api/v1/public/en/posts").param("q", "   ")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/v1/public/en/posts").param("q", "PostgreSQL").param("size", "51")).andExpect(status().isBadRequest());
    }

    @Test
    void exposesEveryEligibleAlternateForABilingualSearchResultWithoutExtraRows() throws Exception {
        mvc.perform(get("/api/v1/public/en/posts").param("q", "Bilingual"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].availableLocales.length()").value(2))
                .andExpect(jsonPath("$.items[0].availableLocales[0]").value("en"))
                .andExpect(jsonPath("$.items[0].availableLocales[1]").value("fa"))
                .andExpect(jsonPath("$.items[0].hreflang[?(@.locale == 'fa')].path").value("/fa/blog/bilingual-search-fa"));
    }

    @Test
    void verifiesThePostgresGinSearchPlan() {
        jdbc.execute("SET enable_seqscan = off");
        String plan = jdbc.queryForObject("EXPLAIN (FORMAT JSON) SELECT id FROM blog_post_translation WHERE search_vector @@ plainto_tsquery('english', ?)", String.class, "PostgreSQL");
        assertThat(plan).contains("ix_blog_post_translation_search_vector");
    }

    private UUID insertPost(UUID category, String locale, String slug, String title, String body, Instant publishedAt) {
        UUID post = UUID.randomUUID();
        jdbc.update("INSERT INTO blog_post (id,category_id,status,published_at,created_at,updated_at,version) VALUES (?,?, 'PUBLISHED', ?, ?, ?, 0)", post, category, time(publishedAt), time(publishedAt), time(publishedAt));
        jdbc.update("INSERT INTO blog_post_translation (id,blog_post_id,language_code,title,slug,excerpt,body_markdown,seo_title,seo_description,created_at,updated_at,version) VALUES (?,?,?,?,?,?,?,?,?,?,?,0)", UUID.randomUUID(), post, locale, title, slug, "excerpt", body, title + " SEO", "description", time(publishedAt), time(publishedAt));
        return post;
    }

    private void insertPostTranslation(UUID post, String locale, String slug, String title, String body, Instant publishedAt) {
        jdbc.update("INSERT INTO blog_post_translation (id,blog_post_id,language_code,title,slug,excerpt,body_markdown,seo_title,seo_description,created_at,updated_at,version) VALUES (?,?,?,?,?,?,?,?,?,?,?,0)", UUID.randomUUID(), post, locale, title, slug, "excerpt", body, title + " SEO", "description", time(publishedAt), time(publishedAt));
    }

    private static Timestamp time(Instant instant) { return Timestamp.from(instant); }
}
