package ir.tahamohamadi.admin;

import com.jayway.jsonpath.JsonPath;
import ir.tahamohamadi.audit.event.AuditEvent;
import ir.tahamohamadi.audit.event.AuditEventRepository;
import ir.tahamohamadi.identity.user.AppUser;
import ir.tahamohamadi.identity.user.AppUserRepository;
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
class AdminSkillIntegrationTest {
    @Container @ServiceConnection static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine");
    @Autowired MockMvc mvc;
    @Autowired AppUserRepository users;
    @Autowired AuditEventRepository audit;

    @Test
    void managesLocalizedSkillCategoriesWithSecurityOrderingVersionsAndAudits() throws Exception {
        AppUser admin = actor("skill-category");
        String payload = categoryPayload("secured-" + UUID.randomUUID(), 0);
        mvc.perform(post("/api/v1/admin/skills/categories").contentType(MediaType.APPLICATION_JSON).content(payload)
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/v1/admin/skills/categories").contentType(MediaType.APPLICATION_JSON).content(payload)
                        .with(SecurityMockMvcRequestPostProcessors.user("reader@example.test").roles("USER"))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/admin/skills/categories").contentType(MediaType.APPLICATION_JSON).content(payload)
                        .with(adminUser(admin)))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/admin/skills/categories").contentType(MediaType.APPLICATION_JSON).content("{}")
                        .with(adminUser(admin)).with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isBadRequest());

        String first = createCategory(admin, "first-" + UUID.randomUUID(), 3);
        String second = createCategory(admin, "second-" + UUID.randomUUID(), 1);
        mvc.perform(get("/api/v1/admin/skills/categories").param("size", "101").with(adminUser(admin)))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/v1/admin/skills/categories").param("sort", "unsupported,desc").with(adminUser(admin)))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/v1/admin/skills/categories").with(adminUser(admin)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.items[0].id").value(second));
        mvc.perform(get("/api/v1/admin/skills/categories/{id}", first).with(adminUser(admin)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.fa.name").value("دسته"))
                .andExpect(jsonPath("$.hibernateLazyInitializer").doesNotExist());
        mvc.perform(get("/api/v1/admin/skills/categories/{id}", UUID.randomUUID()).with(adminUser(admin)))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));

        String original = mvc.perform(get("/api/v1/admin/skills/categories/{id}", first).with(adminUser(admin)))
                .andReturn().getResponse().getContentAsString();
        long version = ((Number) JsonPath.read(original, "$.version")).longValue();
        mvc.perform(put("/api/v1/admin/skills/categories/{id}", first).contentType(MediaType.APPLICATION_JSON)
                        .content(categoryPayload("updated-" + UUID.randomUUID(), 0, version)).with(adminUser(admin))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.sortOrder").value(0));
        mvc.perform(put("/api/v1/admin/skills/categories/{id}", first).contentType(MediaType.APPLICATION_JSON)
                        .content(categoryPayload("stale-" + UUID.randomUUID(), 0, version)).with(adminUser(admin))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("OPTIMISTIC_LOCK_CONFLICT"));
        mvc.perform(delete("/api/v1/admin/skills/categories/{id}", first).param("version", Long.toString(version + 1))
                        .with(adminUser(admin)).with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isNoContent());

        assertAudits(admin, "ADMIN_SKILL_CATEGORY_CREATED", "ADMIN_SKILL_CATEGORY_UPDATED", "ADMIN_SKILL_CATEGORY_DEACTIVATED");
    }

    @Test
    void managesLocalizedSkillsWithBoundedListsSafeConflictsAndAudits() throws Exception {
        AppUser superAdmin = actor("skill");
        String securedPayload = skillPayload(UUID.randomUUID().toString(), "secured-" + UUID.randomUUID(), 0, null);
        mvc.perform(post("/api/v1/admin/skills").contentType(MediaType.APPLICATION_JSON).content(securedPayload)
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/v1/admin/skills").contentType(MediaType.APPLICATION_JSON).content(securedPayload)
                        .with(SecurityMockMvcRequestPostProcessors.user("reader@example.test").roles("USER"))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/admin/skills").contentType(MediaType.APPLICATION_JSON).content(securedPayload)
                        .with(superAdminUser(superAdmin)))
                .andExpect(status().isForbidden());
        String category = createCategory(superAdmin, "engineering-" + UUID.randomUUID(), 0);
        String created = mvc.perform(post("/api/v1/admin/skills").contentType(MediaType.APPLICATION_JSON)
                        .content(skillPayload(category, "java-" + UUID.randomUUID(), 2, null)).with(superAdminUser(superAdmin))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.categoryId").value(category))
                .andExpect(jsonPath("$.en.name").value("Java")).andExpect(jsonPath("$.hibernateLazyInitializer").doesNotExist())
                .andReturn().getResponse().getContentAsString();
        String skill = JsonPath.read(created, "$.id");
        long version = ((Number) JsonPath.read(created, "$.version")).longValue();
        mvc.perform(get("/api/v1/admin/skills").param("sort", "sortOrder,desc").with(superAdminUser(superAdmin)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.items[0].id").value(skill));
        mvc.perform(get("/api/v1/admin/skills/{id}", skill).with(superAdminUser(superAdmin)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.fa.description").value("توضیح"));
        mvc.perform(post("/api/v1/admin/skills").contentType(MediaType.APPLICATION_JSON)
                        .content(skillPayload(UUID.randomUUID().toString(), "missing-" + UUID.randomUUID(), 0, null))
                        .with(superAdminUser(superAdmin)).with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isNotFound());
        mvc.perform(put("/api/v1/admin/skills/{id}", skill).contentType(MediaType.APPLICATION_JSON)
                        .content(skillPayload(category, "java-updated-" + UUID.randomUUID(), 0, version)).with(superAdminUser(superAdmin))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.sortOrder").value(0));
        mvc.perform(put("/api/v1/admin/skills/{id}", skill).contentType(MediaType.APPLICATION_JSON)
                        .content(skillPayload(category, "java-stale-" + UUID.randomUUID(), 0, version)).with(superAdminUser(superAdmin))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("OPTIMISTIC_LOCK_CONFLICT"));
        mvc.perform(delete("/api/v1/admin/skills/{id}", skill).param("version", Long.toString(version + 1))
                        .with(superAdminUser(superAdmin)).with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isNoContent());

        assertAudits(superAdmin, "ADMIN_SKILL_CREATED", "ADMIN_SKILL_UPDATED", "ADMIN_SKILL_DEACTIVATED");
    }

    private String createCategory(AppUser user, String key, int sortOrder) throws Exception {
        String body = mvc.perform(post("/api/v1/admin/skills/categories").contentType(MediaType.APPLICATION_JSON)
                        .content(categoryPayload(key, sortOrder)).with(superAdminUser(user)).with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.id");
    }

    private void assertAudits(AppUser actor, String... actions) {
        List<AuditEvent> events = audit.findByActorIdOrderByOccurredAtDesc(actor.getId());
        assertThat(events).extracting(AuditEvent::getAction).contains(actions);
        assertThat(events).allSatisfy(event -> {
            assertThat(event.getActor().getId()).isEqualTo(actor.getId());
            assertThat(event.getDetails().toString()).doesNotContain("password", "description", "email");
        });
    }

    private static String categoryPayload(String key, int sortOrder) { return categoryPayload(key, sortOrder, null); }
    private static String categoryPayload(String key, int sortOrder, Long version) {
        return "{\"categoryKey\":\"" + key + "\",\"sortOrder\":" + sortOrder + ",\"fa\":{\"name\":\"دسته\",\"description\":\"توضیح\"},\"en\":{\"name\":\"Category\",\"description\":\"Description\"}" + (version == null ? "" : ",\"version\":" + version) + "}";
    }
    private static String skillPayload(String categoryId, String key, int sortOrder, Long version) {
        return "{\"categoryId\":\"" + categoryId + "\",\"skillKey\":\"" + key + "\",\"sortOrder\":" + sortOrder + ",\"fa\":{\"name\":\"جاوا\",\"description\":\"توضیح\"},\"en\":{\"name\":\"Java\",\"description\":\"Description\"}" + (version == null ? "" : ",\"version\":" + version) + "}";
    }
    private AppUser actor(String name) { return users.saveAndFlush(AppUser.create(name + "-" + UUID.randomUUID() + "@example.test", "hash", name, Instant.now())); }
    private static SecurityMockMvcRequestPostProcessors.UserRequestPostProcessor adminUser(AppUser user) { return SecurityMockMvcRequestPostProcessors.user(user.getEmail()).roles("ADMIN"); }
    private static SecurityMockMvcRequestPostProcessors.UserRequestPostProcessor superAdminUser(AppUser user) { return SecurityMockMvcRequestPostProcessors.user(user.getEmail()).roles("SUPER_ADMIN"); }
}
