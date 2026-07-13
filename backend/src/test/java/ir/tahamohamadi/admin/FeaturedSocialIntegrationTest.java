package ir.tahamohamadi.admin;

import com.jayway.jsonpath.JsonPath;
import ir.tahamohamadi.audit.event.AuditEvent;
import ir.tahamohamadi.audit.event.AuditEventRepository;
import ir.tahamohamadi.common.domain.LanguageCode;
import ir.tahamohamadi.content.featured.FeaturedItem;
import ir.tahamohamadi.content.featured.FeaturedItemRepository;
import ir.tahamohamadi.identity.user.AppUser;
import ir.tahamohamadi.identity.user.AppUserRepository;
import ir.tahamohamadi.publication.*;
import org.junit.jupiter.api.Test;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class FeaturedSocialIntegrationTest {
    @Container @ServiceConnection static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine");
    @Autowired MockMvc mvc;
    @Autowired AppUserRepository users;
    @Autowired PublicationRepository publications;
    @Autowired PublicationTranslationRepository publicationTranslations;
    @Autowired FeaturedItemRepository featured;
    @Autowired AuditEventRepository audit;

    @Test
    void administersFeaturedItemsWithPublishedTargetsWindowsVersionsCsrfAndAudits() throws Exception {
        AppUser admin = actor("featured-admin");
        Publication published = publication("published", true);
        Publication draft = publication("draft", false);
        String payload = featuredPayload(published.getId(), null, 2, "2020-01-01T00:00:00Z", null);

        mvc.perform(post("/api/v1/admin/featured-items").contentType(MediaType.APPLICATION_JSON).content(payload)
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/v1/admin/featured-items").contentType(MediaType.APPLICATION_JSON).content(payload)
                        .with(SecurityMockMvcRequestPostProcessors.user("reader@example.test").roles("USER"))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/admin/featured-items").contentType(MediaType.APPLICATION_JSON).content(payload)
                        .with(adminUser(admin))).andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/admin/featured-items").contentType(MediaType.APPLICATION_JSON)
                        .content(featuredPayload(draft.getId(), null, 1, null, null)).with(adminUser(admin))
                        .with(SecurityMockMvcRequestPostProcessors.csrf())).andExpect(status().isBadRequest());

        String created = mvc.perform(post("/api/v1/admin/featured-items").contentType(MediaType.APPLICATION_JSON).content(payload)
                        .with(adminUser(admin)).with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.targetType").value("PUBLICATION"))
                .andExpect(jsonPath("$.hibernateLazyInitializer").doesNotExist()).andReturn().getResponse().getContentAsString();
        String id = JsonPath.read(created, "$.id");
        long version = ((Number) JsonPath.read(created, "$.version")).longValue();
        mvc.perform(get("/api/v1/admin/featured-items").param("size", "101").with(adminUser(admin))).andExpect(status().isBadRequest());

        String updated = mvc.perform(put("/api/v1/admin/featured-items/{id}", id).contentType(MediaType.APPLICATION_JSON)
                        .content(featuredPayload(published.getId(), version, 0, "2020-01-01T00:00:00Z", null)).with(adminUser(admin))
                        .with(SecurityMockMvcRequestPostProcessors.csrf())).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        long updatedVersion = ((Number) JsonPath.read(updated, "$.version")).longValue();
        mvc.perform(put("/api/v1/admin/featured-items/{id}", id).contentType(MediaType.APPLICATION_JSON)
                        .content(featuredPayload(published.getId(), version, 0, null, null)).with(adminUser(admin))
                        .with(SecurityMockMvcRequestPostProcessors.csrf())).andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("OPTIMISTIC_LOCK_CONFLICT"));

        featured.saveAndFlush(FeaturedItem.create(UUID.randomUUID(), "home", null, null, draft, 1, null, null, Instant.now()));
        mvc.perform(get("/api/v1/public/fa/featured").param("slot", "home"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].id").doesNotExist()).andExpect(jsonPath("$.items[0].slug").value("fa-published"));
        mvc.perform(post("/api/v1/admin/featured-items/{id}/deactivate", id).param("version", Long.toString(updatedVersion))
                        .with(adminUser(admin)).with(SecurityMockMvcRequestPostProcessors.csrf())).andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
        assertAudits(admin, "ADMIN_FEATURED_ITEM_CREATED", "ADMIN_FEATURED_ITEM_UPDATED", "ADMIN_FEATURED_ITEM_DEACTIVATED");
    }

    @Test
    void administersOrderedActiveSocialLinksWithCsrfVersionsAuditsAndPublicFiltering() throws Exception {
        AppUser admin = actor("social-admin");
        String created = mvc.perform(post("/api/v1/admin/social-links").contentType(MediaType.APPLICATION_JSON)
                        .content(socialPayload("github", "https://github.com/taha", 1, null)).with(adminUser(admin))
                        .with(SecurityMockMvcRequestPostProcessors.csrf())).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String id = JsonPath.read(created, "$.id"); long version = ((Number) JsonPath.read(created, "$.version")).longValue();
        String second = mvc.perform(post("/api/v1/admin/social-links").contentType(MediaType.APPLICATION_JSON)
                        .content(socialPayload("linkedin", "https://linkedin.com/in/taha", 0, null)).with(adminUser(admin))
                        .with(SecurityMockMvcRequestPostProcessors.csrf())).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        mvc.perform(get("/api/v1/public/social-links")).andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].platformCode").value("linkedin"));
        String updated = mvc.perform(put("/api/v1/admin/social-links/{id}", id).contentType(MediaType.APPLICATION_JSON)
                        .content(socialPayload("github", "https://github.com/tahamohamadi", 1, version)).with(adminUser(admin))
                        .with(SecurityMockMvcRequestPostProcessors.csrf())).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        long updatedVersion = ((Number) JsonPath.read(updated, "$.version")).longValue();
        mvc.perform(put("/api/v1/admin/social-links/{id}", id).contentType(MediaType.APPLICATION_JSON)
                        .content(socialPayload("github", "https://github.com/stale", 1, version)).with(adminUser(admin))
                        .with(SecurityMockMvcRequestPostProcessors.csrf())).andExpect(status().isConflict());
        mvc.perform(post("/api/v1/admin/social-links/{id}/deactivate", id).param("version", Long.toString(updatedVersion))
                        .with(adminUser(admin)).with(SecurityMockMvcRequestPostProcessors.csrf())).andExpect(status().isOk());
        mvc.perform(get("/api/v1/public/social-links")).andExpect(status().isOk()).andExpect(jsonPath("$.items.length()").value(1));
        assertAudits(admin, "ADMIN_SOCIAL_LINK_CREATED", "ADMIN_SOCIAL_LINK_UPDATED", "ADMIN_SOCIAL_LINK_DEACTIVATED");
    }

    private Publication publication(String key, boolean publish) {
        Instant now = Instant.now();
        Publication value = publications.saveAndFlush(Publication.create(UUID.randomUUID(), key, PublicationStage.PUBLISHED, 2025, 0, now));
        publicationTranslations.saveAndFlush(PublicationTranslation.create(UUID.randomUUID(), value, LanguageCode.fa, "FA " + key, "fa-" + key, null, null, null, null, null, now));
        publicationTranslations.saveAndFlush(PublicationTranslation.create(UUID.randomUUID(), value, LanguageCode.en, "EN " + key, "en-" + key, null, null, null, null, null, now));
        if (publish) { value.publish(now); publications.saveAndFlush(value); }
        return value;
    }
    private static String featuredPayload(UUID targetId, Long version, int order, String starts, String ends) { return "{\"slotKey\":\"home\",\"targetType\":\"PUBLICATION\",\"targetId\":\"" + targetId + "\",\"sortOrder\":" + order + ",\"startsAt\":" + (starts == null ? "null" : "\"" + starts + "\"") + ",\"endsAt\":" + (ends == null ? "null" : "\"" + ends + "\"") + (version == null ? "" : ",\"version\":" + version) + "}"; }
    private static String socialPayload(String platform, String url, int order, Long version) { return "{\"platformCode\":\"" + platform + "\",\"url\":\"" + url + "\",\"sortOrder\":" + order + (version == null ? "" : ",\"version\":" + version) + "}"; }
    private AppUser actor(String name) { return users.saveAndFlush(AppUser.create(name + "-" + UUID.randomUUID() + "@example.test", "hash", name, Instant.now())); }
    private void assertAudits(AppUser actor, String... actions) { List<AuditEvent> events = audit.findByActorIdOrderByOccurredAtDesc(actor.getId()); assertThat(events).extracting(AuditEvent::getAction).contains(actions); assertThat(events).allSatisfy(e -> assertThat(e.getActor().getId()).isEqualTo(actor.getId())); }
    private static SecurityMockMvcRequestPostProcessors.UserRequestPostProcessor adminUser(AppUser user) { return SecurityMockMvcRequestPostProcessors.user(user.getEmail()).roles("ADMIN"); }
}
