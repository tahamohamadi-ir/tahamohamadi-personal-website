package ir.tahamohamadi.publicapi;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
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

import jakarta.persistence.EntityManagerFactory;
import static org.assertj.core.api.Assertions.assertThat;
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
    @Autowired EntityManagerFactory entityManagerFactory;

    private final Instant past = Instant.parse("2026-01-01T00:00:00Z");
    private final Instant future = Instant.parse("2099-01-01T00:00:00Z");

    @BeforeEach
    void setUp() {
        jdbc.execute("TRUNCATE TABLE featured_item, social_link, publication_translation, publication, portfolio_project_skill, blog_post_tag, skill_translation, skill, skill_category_translation, skill_category, portfolio_project_translation, portfolio_project, tag_translation, tag, blog_post_translation, blog_post, blog_category_translation, blog_category, content_page_translation, content_page CASCADE");
        insertPage("home", "en", "home", "Home", "PUBLISHED", past, null);
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
    void keepsSameLocaleIneligibleSlugsAsResourceNotFound() throws Exception {
        insertPage("draft-shared-page", "en", "shared-page", "Draft", "DRAFT", null, null);
        insertPage("published-shared-page", "fa", "shared-page", "Published", "PUBLISHED", past, null);
        UUID category = jdbc.queryForObject("SELECT id FROM blog_category WHERE category_key = 'engineering'", UUID.class);
        UUID skill = jdbc.queryForObject("SELECT id FROM skill WHERE skill_key = 'java'", UUID.class);
        insertPost(category, "en", "shared-post", "Draft", "body", "DRAFT", null, null);
        insertPost(category, "fa", "shared-post", "Published", "body", "PUBLISHED", past, null);
        insertProjectWithKey(skill, "draft-shared-project", "en", "shared-project", "Draft", "DRAFT", null);
        insertProjectWithKey(skill, "published-shared-project", "fa", "shared-project", "Published", "PUBLISHED", null);

        mvc.perform(get("/api/v1/public/en/pages/shared-page"))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
        mvc.perform(get("/api/v1/public/en/posts/shared-post"))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
        mvc.perform(get("/api/v1/public/en/portfolio/shared-project"))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void exposesOnlyEligibleAlternateLocalesAndLocalizedCanonicalMetadata() throws Exception {
        insertTranslationForPage("about", "fa", "about-fa", "درباره", null);
        jdbc.update("UPDATE content_page_translation SET canonical_path = ? WHERE slug = ?", "/en/about-me", "about");

        mvc.perform(get("/api/v1/public/en/pages/about"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableLocales.length()").value(2))
                .andExpect(jsonPath("$.availableLocales[0]").value("en"))
                .andExpect(jsonPath("$.availableLocales[1]").value("fa"))
                .andExpect(jsonPath("$.canonicalPath").value("/en/about-me"))
                .andExpect(jsonPath("$.hreflang.length()").value(2))
                .andExpect(jsonPath("$.hreflang[?(@.locale == 'fa')].path").value("/fa/pages/about-fa"));

        mvc.perform(get("/api/v1/public/en/pages/future"))
                .andExpect(status().isNotFound());
    }

    @Test
    void doesNotAdvertiseUnavailableAlternateTranslations() throws Exception {
        mvc.perform(get("/api/v1/public/en/pages/about"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableLocales.length()").value(1))
                .andExpect(jsonPath("$.availableLocales[0]").value("en"))
                .andExpect(jsonPath("$.hreflang.length()").value(1))
                .andExpect(jsonPath("$.hreflang[0].locale").value("en"));
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
    void resolvesHomeByStablePageKeyInTheRequestedLocale() throws Exception {
        insertTranslationForPage("home", "fa", "fa-home", "Persian home", null);

        mvc.perform(get("/api/v1/public/fa/home"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.locale").value("fa"))
                .andExpect(jsonPath("$.page.title").value("Persian home"))
                .andExpect(jsonPath("$.canonicalPath").value("/fa"))
                .andExpect(jsonPath("$.hreflang[?(@.locale == 'fa')].path").value("/fa"));
    }

    @Test
    void boundsTaxonomyAndSkillCollectionsWithDeterministicOrdering() throws Exception {
        for (int index = 1; index <= 55; index++) {
            insertCategory("category-" + index, "en", "category-" + index, "Category " + index, index);
            insertTag("tag-" + index, "en", "tag-" + index, "Tag " + index);
            insertAdditionalSkill("skill-" + index, "Skill " + index, index);
        }

        mvc.perform(get("/api/v1/public/en/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(50))
                .andExpect(jsonPath("$.items[0].slug").value("engineering"))
                .andExpect(jsonPath("$.items[1].slug").value("category-1"));
        mvc.perform(get("/api/v1/public/en/tags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(50))
                .andExpect(jsonPath("$.items[0].slug").value("java"));
        mvc.perform(get("/api/v1/public/en/skills"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(50))
                .andExpect(jsonPath("$.items[0].name").value("Java"));
    }

    @Test
    void countsOnlyEligiblePostsInTheRequestedTaxonomyLocale() throws Exception {
        insertCategory("empty-category", "en", "empty-category", "Empty category", 1);
        insertTag("empty-tag", "en", "empty-tag", "Empty tag");

        mvc.perform(get("/api/v1/public/en/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[?(@.slug == 'engineering')].postCount").value(1))
                .andExpect(jsonPath("$.items[?(@.slug == 'empty-category')].postCount").value(0));
        mvc.perform(get("/api/v1/public/en/tags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[?(@.slug == 'java')].postCount").value(1))
                .andExpect(jsonPath("$.items[?(@.slug == 'empty-tag')].postCount").value(0));
    }

    @Test
    void fetchesMultipleFeaturedTargetsInABoundedNumberOfQueries() throws Exception {
        UUID category = jdbc.queryForObject("SELECT id FROM blog_category WHERE category_key = 'engineering'", UUID.class);
        for (int index = 1; index <= 6; index++) {
            UUID post = insertPost(category, "en", "featured-" + index, "Featured " + index, "body", "PUBLISHED", past, null);
            jdbc.update("INSERT INTO featured_item (id,slot_key,blog_post_id,sort_order,is_active,created_at,updated_at,version) VALUES (?, 'home', ?, ?, true, ?, ?, 0)", UUID.randomUUID(), post, index, time(past), time(past));
        }
        UUID futurePost = insertPost(category, "en", "future-featured", "Future featured", "body", "PUBLISHED", future, null);
        jdbc.update("INSERT INTO featured_item (id,slot_key,blog_post_id,sort_order,is_active,created_at,updated_at,version) VALUES (?, 'home', ?, 0, true, ?, ?, 0)", UUID.randomUUID(), futurePost, time(past), time(past));
        Statistics statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistics.setStatisticsEnabled(true);
        statistics.clear();

        mvc.perform(get("/api/v1/public/en/featured").param("size", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.canonicalPath").value("/en/featured"))
                .andExpect(jsonPath("$.items.length()").value(6))
                .andExpect(jsonPath("$.items[0].slug").value("featured-1"))
                .andExpect(jsonPath("$.items[5].slug").value("featured-6"));

        assertThat(statistics.getPrepareStatementCount()).isLessThanOrEqualTo(2);
    }

    @Test
    void returnsCompleteBoundedHomeContractWithoutIneligibleContentOrInternals() throws Exception {
        UUID publication = insertPublication("research-paper", "en", "research-paper", "Research paper", "PUBLISHED", null);
        jdbc.update("INSERT INTO featured_item (id,slot_key,blog_post_id,sort_order,is_active,created_at,updated_at,version) SELECT ?, 'home', p.id, 0, true, ?, ?, 0 FROM blog_post p JOIN blog_post_translation t ON t.blog_post_id = p.id WHERE t.slug = 'published-post'", UUID.randomUUID(), time(past), time(past));
        jdbc.update("INSERT INTO social_link (id,platform_code,url,sort_order,is_active,created_at,updated_at,version) VALUES (?,?,?,?,true,?,?,0)", UUID.randomUUID(), "github", "https://github.com/taha", 0, time(past), time(past));

        mvc.perform(get("/api/v1/public/en/home"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.title").value("Home"))
                .andExpect(jsonPath("$.featured.items.length()").value(1))
                .andExpect(jsonPath("$.latestPosts.length()").value(1))
                .andExpect(jsonPath("$.selectedProjects.length()").value(1))
                .andExpect(jsonPath("$.selectedPublications.length()").value(1))
                .andExpect(jsonPath("$.skills.items.length()").value(1))
                .andExpect(jsonPath("$.socialLinks.items.length()").value(1))
                .andExpect(jsonPath("$.latestPosts[0].slug").value("published-post"))
                .andExpect(jsonPath("$.selectedPublications[0].slug").value("research-paper"))
                .andExpect(jsonPath("$..id").doesNotExist())
                .andExpect(jsonPath("$..deletedAt").doesNotExist())
                .andExpect(jsonPath("$..storageKey").doesNotExist());
    }

    @Test
    void includesPaginationMetadataForBoundedListContracts() throws Exception {
        mvc.perform(get("/api/v1/public/en/posts").param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(1))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    void rejectsPagesWhoseOffsetsWouldOverflow() throws Exception {
        mvc.perform(get("/api/v1/public/en/posts").param("page", "50000000").param("size", "50"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/v1/public/en/portfolio").param("page", "50000000").param("size", "50"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void reportsTrueTotalsForPostAndProjectPages() throws Exception {
        UUID category = jdbc.queryForObject("SELECT id FROM blog_category WHERE category_key = 'engineering'", UUID.class);
        UUID skill = jdbc.queryForObject("SELECT id FROM skill WHERE skill_key = 'java'", UUID.class);
        for (int index = 1; index <= 20; index++) {
            insertPost(category, "en", "post-" + index, "Post " + index, "body", "PUBLISHED", past, null);
            insertProject(skill, "en", "project-" + index, "Project " + index, "PUBLISHED", null);
        }
        mvc.perform(get("/api/v1/public/en/posts").param("page", "0").param("size", "20"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.items.length()").value(20))
                .andExpect(jsonPath("$.totalElements").value(21)).andExpect(jsonPath("$.totalPages").value(2));
        mvc.perform(get("/api/v1/public/en/posts").param("page", "1").param("size", "20"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(21)).andExpect(jsonPath("$.totalPages").value(2));
        mvc.perform(get("/api/v1/public/en/portfolio").param("page", "0").param("size", "20"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(21)).andExpect(jsonPath("$.totalPages").value(2));
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

    @Test
    void derivesPostAndProjectAlternatesFromEligibleLocalizedTranslations() throws Exception {
        UUID category = jdbc.queryForObject("SELECT id FROM blog_category WHERE category_key = 'engineering'", UUID.class);
        UUID skill = jdbc.queryForObject("SELECT id FROM skill WHERE skill_key = 'java'", UUID.class);
        UUID post = insertPost(category, "en", "bilingual-post", "Bilingual post", "body", "PUBLISHED", past, null);
        UUID project = insertProjectAndReturnId(skill, "en", "bilingual-project", "Bilingual project", "PUBLISHED", null);
        insertPostTranslation(post, "fa", "fa-post", "پست");
        insertProjectTranslation(project, "fa", "fa-project", "پروژه");

        mvc.perform(get("/api/v1/public/en/posts/bilingual-post"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableLocales.length()").value(2))
                .andExpect(jsonPath("$.hreflang[?(@.locale == 'fa')].path").value("/fa/blog/fa-post"));
        mvc.perform(get("/api/v1/public/en/portfolio/bilingual-project"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableLocales.length()").value(2))
                .andExpect(jsonPath("$.hreflang[?(@.locale == 'fa')].path").value("/fa/portfolio/fa-project"));
        mvc.perform(get("/api/v1/public/fa/posts/bilingual-post"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.availableLocales[0]").value("en"));
        mvc.perform(get("/api/v1/public/fa/portfolio/bilingual-project"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.alternatePaths[0]").value("/en/portfolio/bilingual-project"));
    }

    private UUID insertPage(String key, String locale, String slug, String title, String state, Instant publishedAt, Instant deletedAt) {
        UUID id = UUID.randomUUID(); UUID translation = UUID.randomUUID();
        jdbc.update("INSERT INTO content_page (id,page_key,status,published_at,created_at,updated_at,deleted_at,version) VALUES (?,?,?,?,?,?,?,0)", id,key,state,time(publishedAt),time(past),time(past),time(deletedAt));
        jdbc.update("INSERT INTO content_page_translation (id,content_page_id,language_code,title,slug,summary,body_markdown,seo_title,seo_description,created_at,updated_at,version) VALUES (?,?,?,?,?,?,?,?,?,?,?,0)", translation,id,locale,title,slug,"summary","body",title + " SEO","description",time(past),time(past));
        return id;
    }
    private UUID insertCategory(String key,String locale,String slug,String name) { return insertCategory(key, locale, slug, name, 0); }
    private UUID insertCategory(String key,String locale,String slug,String name,int sortOrder) { UUID id=UUID.randomUUID(); jdbc.update("INSERT INTO blog_category (id,category_key,sort_order,is_active,created_at,updated_at,version) VALUES (?,?,?,true,?,?,0)",id,key,sortOrder,time(past),time(past)); jdbc.update("INSERT INTO blog_category_translation (id,blog_category_id,language_code,name,slug,created_at,updated_at,version) VALUES (?,?,?,?,?,?,?,0)",UUID.randomUUID(),id,locale,name,slug,time(past),time(past)); return id; }
    private UUID insertTag(String key,String locale,String slug,String name) { UUID id=UUID.randomUUID(); jdbc.update("INSERT INTO tag (id,tag_key,is_active,created_at,updated_at,version) VALUES (?,?,true,?,?,0)",id,key,time(past),time(past)); jdbc.update("INSERT INTO tag_translation (id,tag_id,language_code,name,slug,created_at,updated_at,version) VALUES (?,?,?,?,?,?,?,0)",UUID.randomUUID(),id,locale,name,slug,time(past),time(past)); return id; }
    private UUID insertPost(UUID category,String locale,String slug,String title,String body,String state,Instant publishedAt,Instant deletedAt) { UUID id=UUID.randomUUID(); jdbc.update("INSERT INTO blog_post (id,category_id,status,published_at,created_at,updated_at,deleted_at,version) VALUES (?,?,?,?,?,?,?,0)",id,category,state,time(publishedAt),time(past),time(past),time(deletedAt)); jdbc.update("INSERT INTO blog_post_translation (id,blog_post_id,language_code,title,slug,excerpt,body_markdown,seo_title,seo_description,created_at,updated_at,version) VALUES (?,?,?,?,?,?,?,?,?,?,?,0)",UUID.randomUUID(),id,locale,title,slug,"excerpt",body,title+" SEO","description",time(past),time(past)); return id; }
    private UUID insertSkill(String key,String locale,String name) { UUID category=UUID.randomUUID(); UUID id=UUID.randomUUID(); jdbc.update("INSERT INTO skill_category (id,category_key,sort_order,is_active,created_at,updated_at,version) VALUES (?,?,0,true,?,?,0)",category,"languages",time(past),time(past)); jdbc.update("INSERT INTO skill_category_translation (id,skill_category_id,language_code,name,created_at,updated_at,version) VALUES (?,?,?,?,?,?,0)",UUID.randomUUID(),category,locale,"Languages",time(past),time(past)); jdbc.update("INSERT INTO skill (id,skill_key,skill_category_id,sort_order,is_active,created_at,updated_at,version) VALUES (?,?,?,0,true,?,?,0)",id,key,category,time(past),time(past)); jdbc.update("INSERT INTO skill_translation (id,skill_id,language_code,name,description,created_at,updated_at,version) VALUES (?,?,?,?,?,?,?,0)",UUID.randomUUID(),id,locale,name,"description",time(past),time(past)); return id; }
    private void insertAdditionalSkill(String key,String name,int sortOrder) { UUID category = jdbc.queryForObject("SELECT id FROM skill_category WHERE category_key = 'languages'", UUID.class); UUID id=UUID.randomUUID(); jdbc.update("INSERT INTO skill (id,skill_key,skill_category_id,sort_order,is_active,created_at,updated_at,version) VALUES (?,?,?, ?,true,?,?,0)",id,key,category,sortOrder,time(past),time(past)); jdbc.update("INSERT INTO skill_translation (id,skill_id,language_code,name,description,created_at,updated_at,version) VALUES (?,?,?,?,?,?,?,0)",UUID.randomUUID(),id,"en",name,"description",time(past),time(past)); }
    private void insertProject(UUID skill,String locale,String slug,String title,String state,Instant deletedAt) { insertProjectWithKey(skill,slug,locale,slug,title,state,deletedAt); }
    private void insertProjectWithKey(UUID skill,String key,String locale,String slug,String title,String state,Instant deletedAt) { UUID id=UUID.randomUUID(); jdbc.update("INSERT INTO portfolio_project (id,project_key,status,started_on,sort_order,created_at,updated_at,deleted_at,version) VALUES (?,?,?,'2025-01-01',0,?,?,?,0)",id,key,state,time(past),time(past),time(deletedAt)); jdbc.update("INSERT INTO portfolio_project_translation (id,portfolio_project_id,language_code,title,slug,summary,body_markdown,seo_title,seo_description,created_at,updated_at,version) VALUES (?,?,?,?,?,?,?,?,?,?,?,0)",UUID.randomUUID(),id,locale,title,slug,"summary","body",title+" SEO","description",time(past),time(past)); jdbc.update("INSERT INTO portfolio_project_skill (portfolio_project_id,skill_id,sort_order) VALUES (?,?,0)",id,skill); }
    private UUID insertProjectAndReturnId(UUID skill,String locale,String slug,String title,String state,Instant deletedAt) { UUID id=UUID.randomUUID(); jdbc.update("INSERT INTO portfolio_project (id,project_key,status,started_on,sort_order,created_at,updated_at,deleted_at,version) VALUES (?,?,?,'2025-01-01',0,?,?,?,0)",id,slug,state,time(past),time(past),time(deletedAt)); jdbc.update("INSERT INTO portfolio_project_translation (id,portfolio_project_id,language_code,title,slug,summary,body_markdown,seo_title,seo_description,created_at,updated_at,version) VALUES (?,?,?,?,?,?,?,?,?,?,?,0)",UUID.randomUUID(),id,locale,title,slug,"summary","body",title+" SEO","description",time(past),time(past)); jdbc.update("INSERT INTO portfolio_project_skill (portfolio_project_id,skill_id,sort_order) VALUES (?,?,0)",id,skill); return id; }
    private void insertPostTranslation(UUID post,String locale,String slug,String title) { jdbc.update("INSERT INTO blog_post_translation (id,blog_post_id,language_code,title,slug,excerpt,body_markdown,seo_title,seo_description,created_at,updated_at,version) VALUES (?,?,?,?,?,?,?,?,?,?,?,0)",UUID.randomUUID(),post,locale,title,slug,"excerpt","body",title+" SEO","description",time(past),time(past)); }
    private void insertProjectTranslation(UUID project,String locale,String slug,String title) { jdbc.update("INSERT INTO portfolio_project_translation (id,portfolio_project_id,language_code,title,slug,summary,body_markdown,seo_title,seo_description,created_at,updated_at,version) VALUES (?,?,?,?,?,?,?,?,?,?,?,0)",UUID.randomUUID(),project,locale,title,slug,"summary","body",title+" SEO","description",time(past),time(past)); }
    private void insertTranslationForPage(String key,String locale,String slug,String title,Instant deletedAt) { jdbc.update("INSERT INTO content_page_translation (id,content_page_id,language_code,title,slug,summary,body_markdown,seo_title,seo_description,created_at,updated_at,deleted_at,version) SELECT ?, id, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0 FROM content_page WHERE page_key = ?", UUID.randomUUID(),locale,title,slug,"summary","body",title+" SEO","description",time(past),time(past),time(deletedAt),key); }
    private UUID insertPublication(String key,String locale,String slug,String title,String status,Instant deletedAt) { UUID id=UUID.randomUUID(); jdbc.update("INSERT INTO publication (id,publication_key,content_status,publication_stage,year,sort_order,created_at,updated_at,deleted_at,version) VALUES (?,?,?,'PUBLISHED',2026,0,?,?,?,0)",id,key,status,time(past),time(past),time(deletedAt)); jdbc.update("INSERT INTO publication_translation (id,publication_id,language_code,title,slug,abstract_text,authors_display,venue_display,seo_title,seo_description,created_at,updated_at,version) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,0)",UUID.randomUUID(),id,locale,title,slug,"abstract","authors","venue",title+" SEO","description",time(past),time(past)); return id; }
    private static Timestamp time(Instant value) { return value == null ? null : Timestamp.from(value); }
}
