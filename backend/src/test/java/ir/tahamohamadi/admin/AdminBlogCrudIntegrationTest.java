package ir.tahamohamadi.admin;

import com.jayway.jsonpath.JsonPath;
import ir.tahamohamadi.blog.category.BlogCategory;
import ir.tahamohamadi.blog.category.BlogCategoryRepository;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class AdminBlogCrudIntegrationTest {
    @Container @ServiceConnection static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine");
    @Autowired MockMvc mvc;
    @Autowired BlogCategoryRepository categories;
    @Autowired AppUserRepository users;

    @Test void securityValidationPaginationAndDtoBoundaries() throws Exception {
        AppUser admin = users.saveAndFlush(AppUser.create("crud-admin-" + UUID.randomUUID() + "@example.test", "hash", "Crud admin", Instant.now()));
        BlogCategory category = categories.save(BlogCategory.create(UUID.randomUUID(), "tech-" + UUID.randomUUID(), 0, Instant.now()));
        String body = payload(category.getId(), null, "En", "en");
        mvc.perform(post("/api/v1/admin/blog/posts").contentType(MediaType.APPLICATION_JSON).content(body).with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/v1/admin/blog/posts").contentType(MediaType.APPLICATION_JSON).content(body).with(SecurityMockMvcRequestPostProcessors.user("user").roles("USER")).with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/admin/blog/posts").contentType(MediaType.APPLICATION_JSON).content(body).with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN")))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/admin/blog/posts").contentType(MediaType.APPLICATION_JSON).content("{}").with(SecurityMockMvcRequestPostProcessors.user(admin.getEmail()).roles("ADMIN")).with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isBadRequest());
        String response = mvc.perform(post("/api/v1/admin/blog/posts").contentType(MediaType.APPLICATION_JSON).content(body).with(SecurityMockMvcRequestPostProcessors.user(admin.getEmail()).roles("ADMIN")).with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.id").isString()).andExpect(jsonPath("$.fa.title").value("Fa")).andExpect(jsonPath("$.en.title").value("En")).andExpect(jsonPath("$.version").value(0)).andExpect(jsonPath("$.hibernateLazyInitializer").doesNotExist()).andReturn().getResponse().getContentAsString();
        String id = JsonPath.read(response, "$.id");
        mvc.perform(get("/api/v1/admin/blog/posts").param("size", "101").with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN")))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/v1/admin/blog/posts").param("size", "1").with(SecurityMockMvcRequestPostProcessors.user("super").roles("SUPER_ADMIN")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.items").isArray()).andExpect(jsonPath("$.page").value(0)).andExpect(jsonPath("$.size").value(1)).andExpect(jsonPath("$.totalElements").isNumber()).andExpect(jsonPath("$.sort").exists());
        mvc.perform(put("/api/v1/admin/blog/posts/{id}", id).contentType(MediaType.APPLICATION_JSON).content(payload(category.getId(), 0L, "Changed", "changed")).with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN")))
                .andExpect(status().isForbidden());
        mvc.perform(put("/api/v1/admin/blog/posts/{id}", id).contentType(MediaType.APPLICATION_JSON).content(payload(category.getId(), 0L, "Changed", "changed")).with(SecurityMockMvcRequestPostProcessors.user(admin.getEmail()).roles("ADMIN")).with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.en.title").value("Changed"));
        mvc.perform(put("/api/v1/admin/blog/posts/{id}", id).contentType(MediaType.APPLICATION_JSON).content(payload(category.getId(), 0L, "Lost", "lost")).with(SecurityMockMvcRequestPostProcessors.user(admin.getEmail()).roles("ADMIN")).with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isConflict());
        String detail = mvc.perform(get("/api/v1/admin/blog/posts/{id}", id).with(SecurityMockMvcRequestPostProcessors.user(admin.getEmail()).roles("ADMIN"))).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertThat(detail).contains("Changed").doesNotContain("storageKey").doesNotContain("auditEvent");
    }

    private static String payload(UUID categoryId, Long version, String enTitle, String enSlug) {
        String versionField = version == null ? "" : ",\"version\":" + version;
        return "{\"categoryId\":\"" + categoryId + "\",\"fa\":{\"title\":\"Fa\",\"slug\":\"fa-" + UUID.randomUUID() + "\",\"bodyMarkdown\":\"body\"},\"en\":{\"title\":\"" + enTitle + "\",\"slug\":\"" + enSlug + "-" + UUID.randomUUID() + "\",\"bodyMarkdown\":\"body\"}" + versionField + "}";
    }
}
