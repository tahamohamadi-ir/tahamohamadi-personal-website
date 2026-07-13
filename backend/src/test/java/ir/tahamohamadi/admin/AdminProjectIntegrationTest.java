package ir.tahamohamadi.admin;

import com.jayway.jsonpath.JsonPath;
import ir.tahamohamadi.audit.event.AuditEvent;
import ir.tahamohamadi.audit.event.AuditEventRepository;
import ir.tahamohamadi.identity.user.AppUser;
import ir.tahamohamadi.identity.user.AppUserRepository;
import ir.tahamohamadi.media.asset.MediaAsset;
import ir.tahamohamadi.media.asset.MediaAssetRepository;
import ir.tahamohamadi.portfolio.project.PortfolioProjectRepository;
import ir.tahamohamadi.skill.Skill;
import ir.tahamohamadi.skill.SkillCategory;
import ir.tahamohamadi.skill.SkillCategoryRepository;
import ir.tahamohamadi.skill.SkillRepository;
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
class AdminProjectIntegrationTest {
    @Container @ServiceConnection static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine");
    @Autowired MockMvc mvc;
    @Autowired AppUserRepository users;
    @Autowired MediaAssetRepository media;
    @Autowired SkillCategoryRepository skillCategories;
    @Autowired SkillRepository skills;
    @Autowired PortfolioProjectRepository projects;
    @Autowired AuditEventRepository audit;

    @Test
    void managesLocalizedProjectsWithValidatedReferencesLifecycleSecurityVersionsAndAudits() throws Exception {
        AppUser admin = actor("project-admin");
        String securedPayload = payload("secured", UUID.randomUUID(), List.of(), null, 3);
        mvc.perform(post("/api/v1/admin/portfolio/projects").contentType(MediaType.APPLICATION_JSON).content(securedPayload)
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/v1/admin/portfolio/projects").contentType(MediaType.APPLICATION_JSON).content(securedPayload)
                        .with(SecurityMockMvcRequestPostProcessors.user("reader@example.test").roles("USER"))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/admin/portfolio/projects").contentType(MediaType.APPLICATION_JSON).content(securedPayload)
                        .with(adminUser(admin)))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/admin/portfolio/projects").contentType(MediaType.APPLICATION_JSON).content("{}")
                        .with(adminUser(admin)).with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isBadRequest());

        MediaAsset cover = asset("cover");
        Skill firstSkill = skill("first", 0);
        Skill secondSkill = skill("second", 1);
        mvc.perform(post("/api/v1/admin/portfolio/projects").contentType(MediaType.APPLICATION_JSON)
                        .content(payload("missing", UUID.randomUUID(), List.of(), null, 0)).with(adminUser(admin))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isNotFound());

        String created = mvc.perform(post("/api/v1/admin/portfolio/projects").contentType(MediaType.APPLICATION_JSON)
                        .content(payload("first", cover.getId(), List.of(new SkillReference(secondSkill.getId(), 1), new SkillReference(firstSkill.getId(), 0)), null, 3))
                        .with(adminUser(admin)).with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fa.title").value("پروژه"))
                .andExpect(jsonPath("$.en.slug").value("en-first"))
                .andExpect(jsonPath("$.skills[0].skillId").value(firstSkill.getId().toString()))
                .andExpect(jsonPath("$.skills[1].skillId").value(secondSkill.getId().toString()))
                .andExpect(jsonPath("$.coverMediaId").value(cover.getId().toString()))
                .andExpect(jsonPath("$.storageKey").doesNotExist())
                .andExpect(jsonPath("$.hibernateLazyInitializer").doesNotExist())
                .andReturn().getResponse().getContentAsString();
        String projectId = JsonPath.read(created, "$.id");
        long version = ((Number) JsonPath.read(created, "$.version")).longValue();

        String second = create(admin, payload("second", cover.getId(), List.of(), null, 1));
        mvc.perform(get("/api/v1/admin/portfolio/projects").param("size", "101").with(adminUser(admin)))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/v1/admin/portfolio/projects").param("sort", "status,desc").with(adminUser(admin)))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/v1/admin/portfolio/projects").param("sort", "sortOrder,asc").with(adminUser(admin)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.items[0].id").value(second));
        mvc.perform(get("/api/v1/admin/portfolio/projects/{id}", projectId).with(adminUser(admin)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.skills[0].sortOrder").value(0));

        String updated = mvc.perform(put("/api/v1/admin/portfolio/projects/{id}", projectId).contentType(MediaType.APPLICATION_JSON)
                        .content(payload("updated", cover.getId(), List.of(new SkillReference(firstSkill.getId(), 0)), version, 0))
                        .with(adminUser(admin)).with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.projectKey").value("updated"))
                .andReturn().getResponse().getContentAsString();
        long updatedVersion = ((Number) JsonPath.read(updated, "$.version")).longValue();
        mvc.perform(put("/api/v1/admin/portfolio/projects/{id}", projectId).contentType(MediaType.APPLICATION_JSON)
                        .content(payload("stale", cover.getId(), List.of(), version, 0)).with(adminUser(admin))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("OPTIMISTIC_LOCK_CONFLICT"));

        String published = mvc.perform(post("/api/v1/admin/portfolio/projects/{id}/publish", projectId)
                        .param("version", Long.toString(updatedVersion)).with(adminUser(admin))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("PUBLISHED"))
                .andReturn().getResponse().getContentAsString();
        long publishedVersion = ((Number) JsonPath.read(published, "$.version")).longValue();
        String archived = mvc.perform(post("/api/v1/admin/portfolio/projects/{id}/archive", projectId)
                        .param("version", Long.toString(publishedVersion)).with(adminUser(admin))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("ARCHIVED"))
                .andReturn().getResponse().getContentAsString();
        long archivedVersion = ((Number) JsonPath.read(archived, "$.version")).longValue();
        mvc.perform(delete("/api/v1/admin/portfolio/projects/{id}", projectId).param("version", Long.toString(archivedVersion))
                        .with(adminUser(admin)).with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isNoContent());
        assertThat(projects.findById(UUID.fromString(projectId)).orElseThrow().getDeletedAt()).isNotNull();
        assertAudits(admin, "ADMIN_PROJECT_CREATED", "ADMIN_PROJECT_UPDATED", "ADMIN_PROJECT_PUBLISHED", "ADMIN_PROJECT_ARCHIVED", "ADMIN_PROJECT_DELETED");
    }

    private String create(AppUser admin, String body) throws Exception {
        return JsonPath.read(mvc.perform(post("/api/v1/admin/portfolio/projects").contentType(MediaType.APPLICATION_JSON).content(body)
                        .with(adminUser(admin)).with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString(), "$.id");
    }

    private String payload(String key, UUID coverMediaId, List<SkillReference> references, Long version, int sortOrder) {
        String skillsField = references.stream().map(reference -> "{\"skillId\":\"" + reference.skillId() + "\",\"sortOrder\":" + reference.sortOrder() + "}")
                .collect(java.util.stream.Collectors.joining(","));
        String versionField = version == null ? "" : ",\"version\":" + version;
        return "{\"projectKey\":\"" + key + "\",\"coverMediaId\":\"" + coverMediaId + "\",\"startedOn\":\"2025-01-01\",\"endedOn\":\"2025-02-01\",\"projectUrl\":\"https://example.test/projects/" + key + "\",\"repositoryUrl\":\"https://github.com/example/" + key + "\",\"sortOrder\":" + sortOrder + ",\"fa\":{\"title\":\"پروژه\",\"slug\":\"fa-" + key + "\",\"summary\":\"خلاصه\",\"bodyMarkdown\":\"متن\",\"seoTitle\":\"سئو\",\"seoDescription\":\"توضیح\"},\"en\":{\"title\":\"Project\",\"slug\":\"en-" + key + "\",\"summary\":\"Summary\",\"bodyMarkdown\":\"Body\",\"seoTitle\":\"SEO\",\"seoDescription\":\"Description\"},\"skills\":[" + skillsField + "]" + versionField + "}";
    }

    private MediaAsset asset(String name) { return media.saveAndFlush(MediaAsset.create(UUID.randomUUID(), "storage-" + name + "-" + UUID.randomUUID(), name + ".png", "png", "image/png", 12, "a".repeat(64), 1, 1, Instant.now())); }
    private Skill skill(String key, int sortOrder) { SkillCategory category = skillCategories.saveAndFlush(SkillCategory.create(UUID.randomUUID(), "category-" + key + "-" + UUID.randomUUID(), 0, Instant.now())); return skills.saveAndFlush(Skill.create(UUID.randomUUID(), "skill-" + key + "-" + UUID.randomUUID(), category, sortOrder, Instant.now())); }
    private AppUser actor(String name) { return users.saveAndFlush(AppUser.create(name + "-" + UUID.randomUUID() + "@example.test", "hash", name, Instant.now())); }
    private void assertAudits(AppUser actor, String... actions) { List<AuditEvent> events = audit.findByActorIdOrderByOccurredAtDesc(actor.getId()); assertThat(events).extracting(AuditEvent::getAction).contains(actions); assertThat(events).allSatisfy(event -> assertThat(event.getDetails().toString()).doesNotContain("password", "bodyMarkdown", "storageKey", "email")); }
    private static SecurityMockMvcRequestPostProcessors.UserRequestPostProcessor adminUser(AppUser user) { return SecurityMockMvcRequestPostProcessors.user(user.getEmail()).roles("ADMIN"); }
    private record SkillReference(UUID skillId, int sortOrder) { }
}
