package ir.tahamohamadi.admin;

import com.jayway.jsonpath.JsonPath;
import ir.tahamohamadi.audit.event.AuditEvent;
import ir.tahamohamadi.audit.event.AuditEventRepository;
import ir.tahamohamadi.identity.user.AppUser;
import ir.tahamohamadi.identity.user.AppUserRepository;
import ir.tahamohamadi.common.domain.LanguageCode;
import ir.tahamohamadi.media.asset.MediaAsset;
import ir.tahamohamadi.media.asset.MediaAssetRepository;
import ir.tahamohamadi.publication.Publication;
import ir.tahamohamadi.publication.PublicationRepository;
import ir.tahamohamadi.publication.PublicationStage;
import ir.tahamohamadi.publication.PublicationTranslation;
import ir.tahamohamadi.publication.PublicationTranslationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
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
class PublicationResumeIntegrationTest {
    @Container @ServiceConnection static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired MockMvc mvc;
    @Autowired AppUserRepository users;
    @Autowired MediaAssetRepository media;
    @Autowired AuditEventRepository audit;
    @Autowired PublicationRepository publications;
    @Autowired PublicationTranslationRepository publicationTranslations;

    @Test
    @Transactional
    void listsPublicationWithMissingTranslationForAdminCompletion() throws Exception {
        AppUser admin = actor("incomplete-publication-admin");
        Instant now = Instant.now();
        Publication publication = publications.saveAndFlush(Publication.create(
                UUID.randomUUID(), "incomplete", PublicationStage.PUBLISHED, 2025, 0, now
        ));
        publicationTranslations.saveAndFlush(PublicationTranslation.create(
                UUID.randomUUID(), publication, LanguageCode.fa, "Incomplete FA", "incomplete-fa",
                null, null, null, null, null, now
        ));

        mvc.perform(get("/api/v1/admin/publications").param("sort", "year,desc").with(adminUser(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value(publication.getId().toString()))
                .andExpect(jsonPath("$.items[0].fa.title").value("Incomplete FA"))
                .andExpect(jsonPath("$.items[0].en").doesNotExist());
    }

    @Test
    void administersLocalizedPublicationsWithSecurityLifecycleVersionsAuditsAndPublicFiltering() throws Exception {
        AppUser admin = actor("publication-admin");
        String payload = publicationPayload("first", null, 2);

        mvc.perform(post("/api/v1/admin/publications").contentType(MediaType.APPLICATION_JSON).content(payload)
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/v1/admin/publications").contentType(MediaType.APPLICATION_JSON).content(payload)
                        .with(SecurityMockMvcRequestPostProcessors.user("reader@example.test").roles("USER"))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/admin/publications").contentType(MediaType.APPLICATION_JSON).content(payload)
                        .with(adminUser(admin)))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/admin/publications").contentType(MediaType.APPLICATION_JSON).content("{}")
                        .with(adminUser(admin)).with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isBadRequest());

        String created = mvc.perform(post("/api/v1/admin/publications").contentType(MediaType.APPLICATION_JSON).content(payload)
                        .with(adminUser(admin)).with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fa.slug").value("fa-first"))
                .andExpect(jsonPath("$.en.title").value("Publication first"))
                .andExpect(jsonPath("$.hibernateLazyInitializer").doesNotExist())
                .andReturn().getResponse().getContentAsString();
        String id = JsonPath.read(created, "$.id");
        long version = ((Number) JsonPath.read(created, "$.version")).longValue();

        mvc.perform(get("/api/v1/admin/publications").param("size", "101").with(adminUser(admin)))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/v1/admin/publications").param("sort", "doi,asc").with(adminUser(admin)))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/v1/admin/publications").param("sort", "year,desc").with(adminUser(admin)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.items[0].id").value(id));
        mvc.perform(get("/api/v1/public/fa/publications").param("size", "50"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.items").isEmpty());

        String updated = mvc.perform(put("/api/v1/admin/publications/{id}", id).contentType(MediaType.APPLICATION_JSON)
                        .content(publicationPayload("updated", version, 1)).with(adminUser(admin))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.publicationKey").value("updated"))
                .andReturn().getResponse().getContentAsString();
        long updatedVersion = ((Number) JsonPath.read(updated, "$.version")).longValue();
        mvc.perform(put("/api/v1/admin/publications/{id}", id).contentType(MediaType.APPLICATION_JSON)
                        .content(publicationPayload("stale", version, 0)).with(adminUser(admin))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("OPTIMISTIC_LOCK_CONFLICT"));
        mvc.perform(get("/api/v1/admin/publications/{id}", id).with(adminUser(admin)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.publicationKey").value("updated"));

        String published = mvc.perform(post("/api/v1/admin/publications/{id}/publish", id).param("version", Long.toString(updatedVersion))
                        .with(adminUser(admin)).with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("PUBLISHED"))
                .andReturn().getResponse().getContentAsString();
        long publishedVersion = ((Number) JsonPath.read(published, "$.version")).longValue();
        mvc.perform(get("/api/v1/public/fa/publications").param("page", "0").param("size", "50"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.locale").value("fa"))
                .andExpect(jsonPath("$.items[0].slug").value("fa-updated"))
                .andExpect(jsonPath("$.items[0].status").doesNotExist())
                .andExpect(jsonPath("$.canonicalPath").value("/fa/publications"))
                .andExpect(jsonPath("$.items[0].canonicalPath").value("/fa/publications/fa-updated"))
                .andExpect(jsonPath("$.items[0].id").doesNotExist());
        mvc.perform(get("/api/v1/public/en/publications/en-updated"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.locale").value("en"))
                .andExpect(jsonPath("$.publicationKey").doesNotExist())
                .andExpect(jsonPath("$.canonicalPath").value("/en/publications/en-updated"))
                .andExpect(jsonPath("$.id").doesNotExist());

        mvc.perform(post("/api/v1/admin/publications/{id}/archive", id).param("version", Long.toString(publishedVersion))
                        .with(adminUser(admin)).with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("ARCHIVED"));
        mvc.perform(get("/api/v1/public/fa/publications").param("size", "50"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.items").isEmpty());
        assertAudits(admin, "ADMIN_PUBLICATION_CREATED", "ADMIN_PUBLICATION_UPDATED", "ADMIN_PUBLICATION_PUBLISHED", "ADMIN_PUBLICATION_ARCHIVED");
    }

    @Test
    void administersLocalizedResumeEntriesAndOneCurrentDocumentPerLocale() throws Exception {
        AppUser admin = actor("resume-admin");
        String entry = mvc.perform(post("/api/v1/admin/resume/entries").contentType(MediaType.APPLICATION_JSON)
                        .content(resumeEntryPayload("first", null, 1)).with(adminUser(admin))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.fa.title").value("رزومه first"))
                .andReturn().getResponse().getContentAsString();
        String entryId = JsonPath.read(entry, "$.id");
        long entryVersion = ((Number) JsonPath.read(entry, "$.version")).longValue();
        mvc.perform(get("/api/v1/public/fa/resume"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.entries").isEmpty());
        String publishedEntry = mvc.perform(post("/api/v1/admin/resume/entries/{id}/publish", entryId)
                        .param("version", Long.toString(entryVersion)).with(adminUser(admin))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("PUBLISHED"))
                .andReturn().getResponse().getContentAsString();
        long publishedEntryVersion = ((Number) JsonPath.read(publishedEntry, "$.version")).longValue();
        mvc.perform(post("/api/v1/admin/resume/entries/{id}/archive", entryId).param("version", Long.toString(publishedEntryVersion))
                        .with(adminUser(admin)).with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("ARCHIVED"));

        MediaAsset first = asset("first-resume");
        String document = mvc.perform(post("/api/v1/admin/resume/documents").contentType(MediaType.APPLICATION_JSON)
                        .content(documentPayload(first.getId(), null)).with(adminUser(admin))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.languageCode").value("fa"))
                .andReturn().getResponse().getContentAsString();
        String documentId = JsonPath.read(document, "$.id");
        long documentVersion = ((Number) JsonPath.read(document, "$.version")).longValue();
        mvc.perform(post("/api/v1/admin/resume/documents/{id}/publish", documentId).param("version", Long.toString(documentVersion))
                        .with(adminUser(admin)).with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("PUBLISHED"));
        mvc.perform(post("/api/v1/admin/resume/documents").contentType(MediaType.APPLICATION_JSON)
                        .content(documentPayload(asset("replacement").getId(), null)).with(adminUser(admin))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("DUPLICATE_RESOURCE"));
        mvc.perform(get("/api/v1/public/fa/resume"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.locale").value("fa"))
                .andExpect(jsonPath("$.entries").isEmpty())
                .andExpect(jsonPath("$.canonicalPath").value("/fa/resume"))
                .andExpect(jsonPath("$.document.mediaUrl").value("/api/v1/public/media/" + first.getId()))
                .andExpect(jsonPath("$.document.storageKey").doesNotExist())
                .andExpect(jsonPath("$.document.id").doesNotExist());
        assertAudits(admin, "ADMIN_RESUME_ENTRY_CREATED", "ADMIN_RESUME_ENTRY_PUBLISHED", "ADMIN_RESUME_ENTRY_ARCHIVED", "ADMIN_RESUME_DOCUMENT_CREATED", "ADMIN_RESUME_DOCUMENT_PUBLISHED");
    }

    private String publicationPayload(String key, Long version, int sortOrder) {
        String versionField = version == null ? "" : ",\"version\":" + version;
        return "{\"publicationKey\":\"" + key + "\",\"publicationStage\":\"PUBLISHED\",\"doi\":\"10.1000/" + key + "\",\"externalUrl\":\"https://example.test/publications/" + key + "\",\"publishedOn\":\"2025-01-01\",\"year\":2025,\"sortOrder\":" + sortOrder + ",\"fa\":{\"title\":\"انتشار " + key + "\",\"slug\":\"fa-" + key + "\",\"abstractText\":\"چکیده\",\"authorsDisplay\":\"Taha\",\"venueDisplay\":\"Journal\",\"seoTitle\":\"SEO\",\"seoDescription\":\"Description\"},\"en\":{\"title\":\"Publication " + key + "\",\"slug\":\"en-" + key + "\",\"abstractText\":\"Abstract\",\"authorsDisplay\":\"Taha\",\"venueDisplay\":\"Journal\",\"seoTitle\":\"SEO\",\"seoDescription\":\"Description\"}" + versionField + "}";
    }

    private String resumeEntryPayload(String key, Long version, int sortOrder) {
        String versionField = version == null ? "" : ",\"version\":" + version;
        return "{\"entryType\":\"EXPERIENCE\",\"startedOn\":\"2020-01-01\",\"endedOn\":null,\"current\":true,\"sortOrder\":" + sortOrder + ",\"fa\":{\"title\":\"رزومه " + key + "\",\"organization\":\"دانشگاه\",\"location\":\"تهران\",\"summary\":\"خلاصه\"},\"en\":{\"title\":\"Resume " + key + "\",\"organization\":\"University\",\"location\":\"Tehran\",\"summary\":\"Summary\"}" + versionField + "}";
    }

    private String documentPayload(UUID mediaId, Long version) {
        return "{\"languageCode\":\"fa\",\"mediaAssetId\":\"" + mediaId + "\"" + (version == null ? "" : ",\"version\":" + version) + "}";
    }

    private MediaAsset asset(String name) {
        return media.saveAndFlush(MediaAsset.create(UUID.randomUUID(), "storage-" + name + "-" + UUID.randomUUID(), name + ".pdf", "pdf", "application/pdf", 12, "a".repeat(64), null, null, Instant.now()));
    }

    private AppUser actor(String name) {
        return users.saveAndFlush(AppUser.create(name + "-" + UUID.randomUUID() + "@example.test", "hash", name, Instant.now()));
    }

    private void assertAudits(AppUser actor, String... actions) {
        List<AuditEvent> events = audit.findByActorIdOrderByOccurredAtDesc(actor.getId());
        assertThat(events).extracting(AuditEvent::getAction).contains(actions);
        assertThat(events).allSatisfy(event -> {
            assertThat(event.getActor().getId()).isEqualTo(actor.getId());
            assertThat(event.getDetails().toString()).doesNotContain("storageKey", "email", "password", "summary", "abstractText");
        });
    }

    private static SecurityMockMvcRequestPostProcessors.UserRequestPostProcessor adminUser(AppUser user) {
        return SecurityMockMvcRequestPostProcessors.user(user.getEmail()).roles("ADMIN");
    }
}
