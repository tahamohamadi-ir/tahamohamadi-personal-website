package ir.tahamohamadi.admin;

import com.jayway.jsonpath.JsonPath;
import ir.tahamohamadi.audit.event.AuditEvent;
import ir.tahamohamadi.audit.event.AuditEventRepository;
import ir.tahamohamadi.common.domain.LanguageCode;
import ir.tahamohamadi.content.page.ContentPage;
import ir.tahamohamadi.content.page.ContentPageRepository;
import ir.tahamohamadi.content.page.ContentPageTranslation;
import ir.tahamohamadi.content.page.ContentPageTranslationRepository;
import ir.tahamohamadi.identity.user.AppUser;
import ir.tahamohamadi.identity.user.AppUserRepository;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AdminPageAuditAndConcurrencyIntegrationTest {
    @Container @ServiceConnection static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired MockMvc mvc;
    @Autowired AppUserRepository users;
    @Autowired ContentPageRepository pages;
    @Autowired ContentPageTranslationRepository translations;
    @Autowired AuditEventRepository audit;
    @Autowired EntityManagerFactory entityManagerFactory;

    @Test
    @Order(1)
    void listsLocalizedPageDtosWithBoundedPagingDeterministicOrderingAndNoTranslationNPlusOne() throws Exception {
        AppUser admin = actor("page-list-admin");
        seed("oldest", Instant.parse("2026-01-01T00:00:00Z"));
        seed("middle", Instant.parse("2026-01-02T00:00:00Z"));
        seed("newest", Instant.parse("2026-01-03T00:00:00Z"));

        Statistics statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistics.setStatisticsEnabled(true);
        statistics.clear();

        mvc.perform(get("/api/v1/admin/pages").param("page", "0").param("size", "3").with(adminUser(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(3))
                .andExpect(jsonPath("$.items[0].pageKey").value("newest"))
                .andExpect(jsonPath("$.items[1].pageKey").value("middle"))
                .andExpect(jsonPath("$.items[2].pageKey").value("oldest"))
                .andExpect(jsonPath("$.items[0].fa.slug").value("fa-newest"))
                .andExpect(jsonPath("$.items[0].en.slug").value("en-newest"))
                .andExpect(jsonPath("$.items[0].createdAt").doesNotExist())
                .andExpect(jsonPath("$.items[0].deletedAt").doesNotExist())
                .andExpect(jsonPath("$.items[0].hibernateLazyInitializer").doesNotExist());
        assertThat(statistics.getPrepareStatementCount()).isLessThanOrEqualTo(3);

        mvc.perform(get("/api/v1/admin/pages").param("size", "101").with(adminUser(admin)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(2)
    void enforcesCsrfAndRbacAndPersistsTheAuthenticatedActorForPageMutations() throws Exception {
        AppUser admin = actor("page-audit-admin");
        String body = payload("audit-page", null);

        mvc.perform(post("/api/v1/admin/pages").contentType(MediaType.APPLICATION_JSON).content(body)
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/v1/admin/pages").contentType(MediaType.APPLICATION_JSON).content(body)
                        .with(SecurityMockMvcRequestPostProcessors.user("reader@example.test").roles("USER"))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/admin/pages").contentType(MediaType.APPLICATION_JSON).content(body).with(adminUser(admin)))
                .andExpect(status().isForbidden());

        String created = mvc.perform(post("/api/v1/admin/pages").contentType(MediaType.APPLICATION_JSON).content(body)
                        .with(adminUser(admin)).with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isString())
                .andReturn().getResponse().getContentAsString();
        UUID pageId = UUID.fromString(JsonPath.read(created, "$.id"));
        long version = ((Number) JsonPath.read(created, "$.version")).longValue();

        mvc.perform(delete("/api/v1/admin/pages/{id}", pageId)
                        .param("version", Long.toString(version))
                        .with(adminUser(admin))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isNoContent());

        ContentPage deleted = pages.findById(pageId).orElseThrow();
        assertThat(deleted.getDeletedBy()).isNotNull();
        assertThat(deleted.getDeletedBy().getId()).isEqualTo(admin.getId());

        List<AuditEvent> events = audit.findByActorIdOrderByOccurredAtDesc(admin.getId());
        assertThat(events).anySatisfy(event -> {
            assertThat(event.getAction()).isEqualTo("ADMIN_PAGE_CREATED");
            assertThat(event.getTargetId()).isEqualTo(pageId);
            assertThat(event.getActor().getId()).isEqualTo(admin.getId());
        });
        assertThat(events).anySatisfy(event -> {
            assertThat(event.getAction()).isEqualTo("ADMIN_PAGE_DELETED");
            assertThat(event.getTargetId()).isEqualTo(pageId);
            assertThat(event.getActor().getId()).isEqualTo(admin.getId());
        });
    }

    @Test
    @Order(3)
    void returnsSafeConflictForStalePageUpdates() throws Exception {
        AppUser admin = actor("page-version-admin");
        String created = mvc.perform(post("/api/v1/admin/pages").contentType(MediaType.APPLICATION_JSON).content(payload("versioned-page", null))
                        .with(adminUser(admin)).with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String pageId = JsonPath.read(created, "$.id");
        long version = ((Number) JsonPath.read(created, "$.version")).longValue();

        mvc.perform(put("/api/v1/admin/pages/{id}", pageId).contentType(MediaType.APPLICATION_JSON).content(payload("versioned-page-updated", version))
                        .with(adminUser(admin)).with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk());
        mvc.perform(put("/api/v1/admin/pages/{id}", pageId).contentType(MediaType.APPLICATION_JSON).content(payload("stale-page", version))
                        .with(adminUser(admin)).with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("OPTIMISTIC_LOCK_CONFLICT"))
                .andExpect(jsonPath("$.message").value("The resource was changed by another request"))
                .andExpect(jsonPath("$.exception").doesNotExist())
                .andExpect(jsonPath("$.stackTrace").doesNotExist());
        mvc.perform(get("/api/v1/admin/pages/{id}", pageId).with(adminUser(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageKey").value("versioned-page-updated"));
    }

    private void seed(String pageKey, Instant createdAt) {
        ContentPage page = pages.saveAndFlush(ContentPage.create(UUID.randomUUID(), pageKey, createdAt));
        translations.saveAllAndFlush(List.of(
                translation(page, LanguageCode.fa, "fa-" + pageKey, createdAt),
                translation(page, LanguageCode.en, "en-" + pageKey, createdAt)
        ));
    }

    private ContentPageTranslation translation(ContentPage page, LanguageCode language, String slug, Instant createdAt) {
        ContentPageTranslation translation = ContentPageTranslation.create(UUID.randomUUID(), page, language, language.name() + " title", slug, createdAt);
        translation.update(language.name() + " title", slug, "summary", "body", "seo title", "seo description", "/" + language + "/" + slug);
        return translation;
    }

    private AppUser actor(String prefix) {
        return users.saveAndFlush(AppUser.create(prefix + "-" + UUID.randomUUID() + "@example.test", "hash", prefix, Instant.now()));
    }

    private String payload(String key, Long version) {
        String versionField = version == null ? "" : ",\"version\":" + version;
        return "{\"pageKey\":\"" + key + "\",\"fa\":{\"title\":\"FA " + key + "\",\"slug\":\"fa-" + key + "\",\"seoTitle\":\"FA SEO\",\"seoDescription\":\"FA description\"},\"en\":{\"title\":\"EN " + key + "\",\"slug\":\"en-" + key + "\",\"seoTitle\":\"EN SEO\",\"seoDescription\":\"EN description\"}" + versionField + "}";
    }

    private static SecurityMockMvcRequestPostProcessors.UserRequestPostProcessor adminUser(AppUser user) {
        return SecurityMockMvcRequestPostProcessors.user(user.getEmail()).roles("ADMIN");
    }
}
