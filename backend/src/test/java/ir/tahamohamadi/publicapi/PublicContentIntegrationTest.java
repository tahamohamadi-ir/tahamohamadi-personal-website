package ir.tahamohamadi.publicapi;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
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
class PublicContentIntegrationTest {
    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("public_content_test").withUsername("test").withPassword("test");

    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;

    private final Instant past = Instant.parse("2026-01-01T00:00:00Z");
    private final Instant future = Instant.parse("2099-01-01T00:00:00Z");

    @BeforeEach
    void setUp() {
        jdbc.execute("TRUNCATE TABLE portfolio_project_skill, blog_post_tag, skill_translation, skill, skill_category_translation, skill_category, portfolio_project_translation, portfolio_project, tag_translation, tag, blog_post_translation, blog_post, blog_category_translation, blog_category, content_page_translation, content_page CASCADE");
        insertPage("about", "en", "about", "About", "PUBLISHED", past, null);
        insertPage("draft", "en", "draft", "Draft", "DRAFT", null, null);
        insertPage("future", "en", "future", "Future", "PUBLISHED", future, null);
        insertPage("deleted", "en", "deleted", "Deleted", "PUBLISHED", past, past);

        UUID category = insertCategory("engineering", "en", "engineering", "Engineering");
        UUID tag = insertTag("java", "en", "java", "Java");
        UUID published = insertPost(category, "en", "published-post", "Published post", "searchable body", "PUBLISHED", past, null);
        jdbc.update("INSERT INTO blog_post_tag (blog_post_id, tag_id) VALUES (?, ?)", published, tag);
        insertPost(category, "en", "draft-post", "Draft", "body", "DRAFT", null, null);
        insertPost(category, "en", "future-post", "Future", "body", "PUBLISHED", future, null);
        insertPost(category, "en", "deleted-post", "Deleted", "body", "PUBLISHED", past, past);

        UUID skill = insertSkill("java", "en", "Java");
        insertProject(skill, "en", "portfolio-item", "Portfolio item", "PUBLISHED", null);
        insertProject(skill, "en", "draft-project", "Draft project", "DRAFT", null);
    }

    @Test
    void returnsLocalizedPageWithSeoAndCanonicalMetadata() throws Exception {
        mvc.perform(get("/api/v1/public/en/pages/about"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.locale").value("en"))
                .andExpect(jsonPath("$.availableLocales[0]").value("en"))
                .andExpect(jsonPath("$.canonicalPath").value("/en/pages/about"))
                .andExpect(jsonPath("$.seo.title").value("About SEO"));
    }

    @Test
    void reportsMissingTranslationWithoutFallback() throws Exception {
        mvc.perform(get("/api/v1/public/fa/pages/about"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("TRANSLATION_UNAVAILABLE"))
                .andExpect(jsonPath("$.availableLocales[0]").value("en"));
    }

    @Test
    void exposesOnlyPublishedCurrentNondeletedPostsAndSupportsAllowlistedFilters() throws Exception {
        mvc.perform(get("/api/v1/public/en/posts").param("category", "engineering").param("tag", "java").param("q", "searchable"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].slug").value("published-post"));
    }

    @Test
    void validatesLocaleAndPageSize() throws Exception {
        mvc.perform(get("/api/v1/public/de/posts")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/v1/public/en/posts").param("size", "51")).andExpect(status().isBadRequest());
    }

    @Test
    void exposesLocalizedTaxonomiesSkillsPortfolioAndHome() throws Exception {
        mvc.perform(get("/api/v1/public/en/categories")).andExpect(status().isOk()).andExpect(jsonPath("$.items[0].slug").value("engineering"));
        mvc.perform(get("/api/v1/public/en/tags")).andExpect(status().isOk()).andExpect(jsonPath("$.items[0].slug").value("java"));
        mvc.perform(get("/api/v1/public/en/skills")).andExpect(status().isOk()).andExpect(jsonPath("$.items[0].name").value("Java"));
        mvc.perform(get("/api/v1/public/en/portfolio").param("skill", "java")).andExpect(status().isOk()).andExpect(jsonPath("$.items[0].slug").value("portfolio-item"));
        mvc.perform(get("/api/v1/public/en/home")).andExpect(status().isOk()).andExpect(jsonPath("$.locale").value("en"));
    }

    @Test
    void exposesOnlyPublishedLocalizedPostAndPortfolioDetails() throws Exception {
        mvc.perform(get("/api/v1/public/en/posts/published-post"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.bodyMarkdown").value("searchable body"));
        mvc.perform(get("/api/v1/public/en/portfolio/portfolio-item"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.bodyMarkdown").value("body"));
        mvc.perform(get("/api/v1/public/en/posts/draft-post")).andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/public/en/portfolio/draft-project")).andExpect(status().isNotFound());
    }

    private UUID insertPage(String key, String locale, String slug, String title, String state, Instant publishedAt, Instant deletedAt) {
        UUID id = UUID.randomUUID(); UUID translation = UUID.randomUUID();
        jdbc.update("INSERT INTO content_page (id,page_key,status,published_at,created_at,updated_at,deleted_at,version) VALUES (?,?,?,?,?,?,?,0)", id,key,state,time(publishedAt),time(past),time(past),time(deletedAt));
        jdbc.update("INSERT INTO content_page_translation (id,content_page_id,language_code,title,slug,summary,body_markdown,seo_title,seo_description,created_at,updated_at,version) VALUES (?,?,?,?,?,?,?,?,?,?,?,0)", translation,id,locale,title,slug,"summary","body",title + " SEO","description",time(past),time(past));
        return id;
    }
    private UUID insertCategory(String key,String locale,String slug,String name) { UUID id=UUID.randomUUID(); jdbc.update("INSERT INTO blog_category (id,category_key,sort_order,is_active,created_at,updated_at,version) VALUES (?,?,0,true,?,?,0)",id,key,time(past),time(past)); jdbc.update("INSERT INTO blog_category_translation (id,blog_category_id,language_code,name,slug,created_at,updated_at,version) VALUES (?,?,?,?,?,?,?,0)",UUID.randomUUID(),id,locale,name,slug,time(past),time(past)); return id; }
    private UUID insertTag(String key,String locale,String slug,String name) { UUID id=UUID.randomUUID(); jdbc.update("INSERT INTO tag (id,tag_key,is_active,created_at,updated_at,version) VALUES (?,?,true,?,?,0)",id,key,time(past),time(past)); jdbc.update("INSERT INTO tag_translation (id,tag_id,language_code,name,slug,created_at,updated_at,version) VALUES (?,?,?,?,?,?,?,0)",UUID.randomUUID(),id,locale,name,slug,time(past),time(past)); return id; }
    private UUID insertPost(UUID category,String locale,String slug,String title,String body,String state,Instant publishedAt,Instant deletedAt) { UUID id=UUID.randomUUID(); jdbc.update("INSERT INTO blog_post (id,category_id,status,published_at,created_at,updated_at,deleted_at,version) VALUES (?,?,?,?,?,?,?,0)",id,category,state,time(publishedAt),time(past),time(past),time(deletedAt)); jdbc.update("INSERT INTO blog_post_translation (id,blog_post_id,language_code,title,slug,excerpt,body_markdown,seo_title,seo_description,created_at,updated_at,version) VALUES (?,?,?,?,?,?,?,?,?,?,?,0)",UUID.randomUUID(),id,locale,title,slug,"excerpt",body,title+" SEO","description",time(past),time(past)); return id; }
    private UUID insertSkill(String key,String locale,String name) { UUID category=UUID.randomUUID(); UUID id=UUID.randomUUID(); jdbc.update("INSERT INTO skill_category (id,category_key,sort_order,is_active,created_at,updated_at,version) VALUES (?,?,0,true,?,?,0)",category,"languages",time(past),time(past)); jdbc.update("INSERT INTO skill_category_translation (id,skill_category_id,language_code,name,created_at,updated_at,version) VALUES (?,?,?,?,?,?,0)",UUID.randomUUID(),category,locale,"Languages",time(past),time(past)); jdbc.update("INSERT INTO skill (id,skill_key,skill_category_id,sort_order,is_active,created_at,updated_at,version) VALUES (?,?,?,0,true,?,?,0)",id,key,category,time(past),time(past)); jdbc.update("INSERT INTO skill_translation (id,skill_id,language_code,name,description,created_at,updated_at,version) VALUES (?,?,?,?,?,?,?,0)",UUID.randomUUID(),id,locale,name,"description",time(past),time(past)); return id; }
    private void insertProject(UUID skill,String locale,String slug,String title,String state,Instant deletedAt) { UUID id=UUID.randomUUID(); jdbc.update("INSERT INTO portfolio_project (id,project_key,status,started_on,sort_order,created_at,updated_at,deleted_at,version) VALUES (?,?,?,'2025-01-01',0,?,?,?,0)",id,slug,state,time(past),time(past),time(deletedAt)); jdbc.update("INSERT INTO portfolio_project_translation (id,portfolio_project_id,language_code,title,slug,summary,body_markdown,seo_title,seo_description,created_at,updated_at,version) VALUES (?,?,?,?,?,?,?,?,?,?,?,0)",UUID.randomUUID(),id,locale,title,slug,"summary","body",title+" SEO","description",time(past),time(past)); jdbc.update("INSERT INTO portfolio_project_skill (portfolio_project_id,skill_id,sort_order) VALUES (?,?,0)",id,skill); }
    private static Timestamp time(Instant value) { return value == null ? null : Timestamp.from(value); }
}
