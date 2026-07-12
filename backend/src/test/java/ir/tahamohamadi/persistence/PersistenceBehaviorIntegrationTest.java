package ir.tahamohamadi.persistence;

import ir.tahamohamadi.blog.post.BlogPostTranslation;
import ir.tahamohamadi.blog.post.BlogPostTranslationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, properties = "spring.jpa.hibernate.ddl-auto=validate")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class PersistenceBehaviorIntegrationTest {

    @Container @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired JdbcTemplate jdbc;
    @Autowired BlogPostTranslationRepository posts;

    @Test
    @Transactional
    void publicSearchFiltersDraftAndDeletedRowsAndUsesTheGinIndex() {
        UUID category = insertCategory();
        UUID published = insertPost(category, "PUBLISHED", Instant.parse("2026-01-01T00:00:00Z"), null);
        UUID draft = insertPost(category, "DRAFT", null, null);
        UUID deleted = insertPost(category, "PUBLISHED", Instant.parse("2026-01-02T00:00:00Z"), Instant.now());
        insertTranslation(published, "en", "spring-persistence", "Spring persistence", "Durable PostgreSQL search");
        insertTranslation(draft, "en", "draft-search", "Draft persistence", "must not appear");
        insertTranslation(deleted, "en", "deleted-search", "Deleted persistence", "must not appear");
        insertTranslation(published, "fa", "پایداری", "پایداری داده", "جستجوی فارسی");

        List<BlogPostTranslation> english = posts.searchPublishedByLanguage("en", "persistence");
        List<BlogPostTranslation> persian = posts.searchPublishedByLanguage("fa", "پایداری");

        assertThat(english).extracting(BlogPostTranslation::getId).hasSize(1);
        assertThat(persian).extracting(BlogPostTranslation::getId).hasSize(1);
        jdbc.execute("SET LOCAL enable_seqscan = off");
        String plan = jdbc.queryForObject("EXPLAIN (FORMAT JSON) SELECT * FROM blog_post_translation WHERE search_vector @@ plainto_tsquery('english', 'persistence')", String.class);
        assertThat(plan).contains("ix_blog_post_translation_search_vector");
    }

    @Test
    void databaseConstraintMatrixRejectsInvalidLocaleOrderAndFeaturedTargets() {
        UUID asset = UUID.randomUUID();
        Instant now = Instant.now();
        jdbc.update("INSERT INTO media_asset(id,storage_key,original_filename,extension,mime_type,size_bytes,checksum_sha256,status,created_at,updated_at,version) VALUES(?,?,?,?,?,?,?,?,?,?,0)", asset,"asset-"+asset,"a.png","png","image/png",1L,"a".repeat(64),"ACTIVE",timestamp(now),timestamp(now));
        assertThatThrownBy(() -> jdbc.update("INSERT INTO media_asset_translation(id,media_asset_id,language_code,alt_text,created_at,updated_at,version) VALUES(?,?,?,?,?,?,0)",UUID.randomUUID(),asset,"de","alt",timestamp(now),timestamp(now))).isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> jdbc.update("INSERT INTO skill_category(id,category_key,sort_order,is_active,created_at,updated_at,version) VALUES(?,?,?,?,?,?,0)",UUID.randomUUID(),"negative-"+UUID.randomUUID(),-1,true,timestamp(now),timestamp(now))).isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> jdbc.update("INSERT INTO featured_item(id,slot_key,sort_order,is_active,created_at,updated_at,version) VALUES(?,?,?,?,?,?,0)",UUID.randomUUID(),"home",0,true,timestamp(now),timestamp(now))).isInstanceOf(DataIntegrityViolationException.class);
    }

    private UUID insertCategory() {
        UUID id = UUID.randomUUID(); Instant now = Instant.now();
        jdbc.update("INSERT INTO blog_category(id,category_key,sort_order,is_active,created_at,updated_at,version) VALUES(?,?,?,?,?,?,0)",id,"category-"+id,0,true,timestamp(now),timestamp(now));
        return id;
    }

    private UUID insertPost(UUID category, String status, Instant publishedAt, Instant deletedAt) {
        UUID id = UUID.randomUUID(); Instant now = Instant.now();
        jdbc.update("INSERT INTO blog_post(id,category_id,status,published_at,created_at,updated_at,deleted_at,version) VALUES(?,?,?,?,?,?,?,0)",id,category,status,publishedAt == null ? null : timestamp(publishedAt),timestamp(now),timestamp(now),deletedAt == null ? null : timestamp(deletedAt));
        return id;
    }

    private void insertTranslation(UUID post, String language, String slug, String title, String body) {
        Instant now = Instant.now();
        jdbc.update("INSERT INTO blog_post_translation(id,blog_post_id,language_code,title,slug,body_markdown,created_at,updated_at,version) VALUES(?,?,?,?,?,?,?,?,0)",UUID.randomUUID(),post,language,title,slug,body,timestamp(now),timestamp(now));
    }

    private java.sql.Timestamp timestamp(Instant instant) { return java.sql.Timestamp.from(instant); }
}
