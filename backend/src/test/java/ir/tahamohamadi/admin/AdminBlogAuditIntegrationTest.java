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
class AdminBlogAuditIntegrationTest {
    @Container @ServiceConnection static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine");
    @Autowired MockMvc mvc;
    @Autowired AppUserRepository users;
    @Autowired AuditEventRepository audit;

    @Test
    void managesLocalizedCategoriesAndTagsWithPersistedSanitizedAudits() throws Exception {
        AppUser superAdmin = users.saveAndFlush(AppUser.create("taxonomy-" + UUID.randomUUID() + "@example.test", "hash", "Taxonomy", Instant.now()));
        String securedCategoryPayload = categoryPayload("secured-" + UUID.randomUUID(), 0);
        mvc.perform(post("/api/v1/admin/blog/categories").contentType(MediaType.APPLICATION_JSON).content(securedCategoryPayload).with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/v1/admin/blog/categories").contentType(MediaType.APPLICATION_JSON).content(securedCategoryPayload).with(SecurityMockMvcRequestPostProcessors.user("reader@example.test").roles("USER")).with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/admin/blog/tags").contentType(MediaType.APPLICATION_JSON).content(tagPayload("secured-tag-" + UUID.randomUUID())).with(superAdminUser(superAdmin)))
                .andExpect(status().isForbidden());
        String category = create("/api/v1/admin/blog/categories", categoryPayload("category-" + UUID.randomUUID(), 2), superAdmin)
                .andExpect(status().isCreated()).andExpect(jsonPath("$.fa.name").value("دسته"))
                .andExpect(jsonPath("$.hibernateLazyInitializer").doesNotExist()).andReturn().getResponse().getContentAsString();
        String categoryId = JsonPath.read(category, "$.id");
        long categoryVersion = ((Number) JsonPath.read(category, "$.version")).longValue();
        mvc.perform(put("/api/v1/admin/blog/categories/{id}", categoryId).contentType(MediaType.APPLICATION_JSON)
                        .content(categoryPayload("changed-" + UUID.randomUUID(), 1, categoryVersion)).with(superAdminUser(superAdmin)).with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.sortOrder").value(1));
        mvc.perform(put("/api/v1/admin/blog/categories/{id}", categoryId).contentType(MediaType.APPLICATION_JSON)
                        .content(categoryPayload("stale-" + UUID.randomUUID(), 1, categoryVersion)).with(superAdminUser(superAdmin)).with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("OPTIMISTIC_LOCK_CONFLICT"));
        mvc.perform(get("/api/v1/admin/blog/categories").with(superAdminUser(superAdmin)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.items[0].id").value(categoryId));
        mvc.perform(delete("/api/v1/admin/blog/categories/{id}", categoryId).param("version", "1")
                        .with(superAdminUser(superAdmin)).with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isNoContent());

        String tag = create("/api/v1/admin/blog/tags", tagPayload("tag-" + UUID.randomUUID()), superAdmin)
                .andExpect(status().isCreated()).andExpect(jsonPath("$.en.name").value("Tag"))
                .andReturn().getResponse().getContentAsString();
        String tagId = JsonPath.read(tag, "$.id");
        long tagVersion = ((Number) JsonPath.read(tag, "$.version")).longValue();
        mvc.perform(put("/api/v1/admin/blog/tags/{id}", tagId).contentType(MediaType.APPLICATION_JSON)
                        .content(tagPayload("changed-tag-" + UUID.randomUUID(), tagVersion)).with(superAdminUser(superAdmin)).with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk());
        mvc.perform(delete("/api/v1/admin/blog/tags/{id}", tagId).param("version", Long.toString(tagVersion + 1))
                        .with(superAdminUser(superAdmin)).with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isNoContent());

        List<AuditEvent> events = audit.findByActorIdOrderByOccurredAtDesc(superAdmin.getId());
        assertThat(events).extracting(AuditEvent::getAction).contains("ADMIN_BLOG_CATEGORY_CREATED", "ADMIN_BLOG_CATEGORY_UPDATED", "ADMIN_BLOG_CATEGORY_DEACTIVATED", "ADMIN_TAG_CREATED", "ADMIN_TAG_UPDATED", "ADMIN_TAG_DEACTIVATED");
        assertThat(events).allSatisfy(event -> {
            assertThat(event.getActor().getId()).isEqualTo(superAdmin.getId());
            assertThat(event.getDetails().toString()).doesNotContain("password", "storageKey", "bodyMarkdown", "email");
        });
    }

    private org.springframework.test.web.servlet.ResultActions create(String path, String body, AppUser user) throws Exception {
        return mvc.perform(post(path).contentType(MediaType.APPLICATION_JSON).content(body).with(superAdminUser(user)).with(SecurityMockMvcRequestPostProcessors.csrf()));
    }

    private static String categoryPayload(String key, int sortOrder) { return categoryPayload(key, sortOrder, null); }
    private static String categoryPayload(String key, int sortOrder, Long version) { return "{\"categoryKey\":\"" + key + "\",\"sortOrder\":" + sortOrder + ",\"fa\":{\"name\":\"دسته\",\"slug\":\"fa-" + UUID.randomUUID() + "\",\"seoTitle\":\"SEO\",\"seoDescription\":\"Description\"},\"en\":{\"name\":\"Category\",\"slug\":\"en-" + UUID.randomUUID() + "\",\"seoTitle\":\"SEO\",\"seoDescription\":\"Description\"}" + (version == null ? "" : ",\"version\":" + version) + "}"; }
    private static String tagPayload(String key) { return tagPayload(key, null); }
    private static String tagPayload(String key, Long version) { return "{\"tagKey\":\"" + key + "\",\"fa\":{\"name\":\"برچسب\",\"slug\":\"fa-" + UUID.randomUUID() + "\"},\"en\":{\"name\":\"Tag\",\"slug\":\"en-" + UUID.randomUUID() + "\"}" + (version == null ? "" : ",\"version\":" + version) + "}"; }
    private static SecurityMockMvcRequestPostProcessors.UserRequestPostProcessor superAdminUser(AppUser user) { return SecurityMockMvcRequestPostProcessors.user(user.getEmail()).roles("SUPER_ADMIN"); }
}
