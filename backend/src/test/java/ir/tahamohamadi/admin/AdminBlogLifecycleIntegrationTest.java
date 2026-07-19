package ir.tahamohamadi.admin;

import com.jayway.jsonpath.JsonPath;
import ir.tahamohamadi.blog.category.BlogCategory;
import ir.tahamohamadi.blog.category.BlogCategoryRepository;
import ir.tahamohamadi.blog.post.BlogPostRepository;
import ir.tahamohamadi.blog.post.BlogPostMediaUsage;
import ir.tahamohamadi.blog.tag.Tag;
import ir.tahamohamadi.blog.tag.TagRepository;
import ir.tahamohamadi.media.asset.MediaAsset;
import ir.tahamohamadi.media.asset.MediaAssetRepository;
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
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.UUID;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class AdminBlogLifecycleIntegrationTest {
    @Container @ServiceConnection static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine");
    @Autowired MockMvc mvc;
    @Autowired BlogCategoryRepository categories;
    @Autowired BlogPostRepository posts;
    @Autowired TagRepository tags;
    @Autowired MediaAssetRepository media;
    @Autowired AuditEventRepository audit;
    @Autowired AppUserRepository users;
    @Autowired JdbcTemplate jdbc;

    @Test
    void enforcesLifecycleRulesSecurityCsrfAndOptimisticVersions() throws Exception {
        AppUser admin = actor("lifecycle-admin");
        BlogCategory category = categories.saveAndFlush(BlogCategory.create(UUID.randomUUID(), "lifecycle-" + UUID.randomUUID(), 0, Instant.now()));
        Tag tag = tags.saveAndFlush(Tag.create(UUID.randomUUID(), "tag-" + UUID.randomUUID(), Instant.now()));
        MediaAsset later = asset("later");
        MediaAsset first = asset("first");
        String id = createPost(admin, category.getId(), false, List.of(tag.getId()), List.of(new MediaReference(later.getId(), "ATTACHMENT", 1), new MediaReference(first.getId(), "ATTACHMENT", 0)));

        mvc.perform(post("/api/v1/admin/blog/posts/{id}/publish", id).param("version", "0").with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/v1/admin/blog/posts/{id}/publish", id).param("version", "0")
                        .with(SecurityMockMvcRequestPostProcessors.user("reader@example.test").roles("USER"))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/admin/blog/posts/{id}/publish", id).param("version", "0")
                        .with(adminUser(admin)))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/admin/blog/posts/{id}/publish", id).param("version", "0")
                        .with(adminUser(admin)).with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("PUBLISH_VALIDATION_FAILED"))
                .andExpect(jsonPath("$.message").value("The post does not meet publishing requirements"));
        mvc.perform(post("/api/v1/admin/blog/posts/{id}/archive", id).param("version", "0")
                        .with(adminUser(admin)).with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("STATE_CONFLICT"));

        long updatedVersion = updatePost(admin, id, category.getId(), 0, true);
        String published = mvc.perform(post("/api/v1/admin/blog/posts/{id}/publish", id).param("version", Long.toString(updatedVersion))
                        .with(adminUser(admin)).with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("PUBLISHED"))
                .andReturn().getResponse().getContentAsString();
        long publishedVersion = ((Number) JsonPath.read(published, "$.version")).longValue();
        mvc.perform(post("/api/v1/admin/blog/posts/{id}/publish", id).param("version", Long.toString(publishedVersion))
                        .with(adminUser(admin)).with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("STATE_CONFLICT"));
        mvc.perform(post("/api/v1/admin/blog/posts/{id}/archive", id).param("version", Long.toString(publishedVersion))
                        .with(adminUser(admin)).with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("ARCHIVED"));

        String second = createPost(admin, category.getId(), true, List.of(), List.of());
        mvc.perform(post("/api/v1/admin/blog/posts/{id}/publish", second).param("version", "999")
                        .with(adminUser(admin)).with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("OPTIMISTIC_LOCK_CONFLICT"))
                .andExpect(jsonPath("$.message").value("The resource was changed by another request"));
        mvc.perform(delete("/api/v1/admin/blog/posts/{id}", id).param("version", Long.toString(publishedVersion + 1))
                        .with(adminUser(admin)).with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isNoContent());
        assertThat(posts.findById(UUID.fromString(id)).orElseThrow().getDeletedAt()).isNotNull();
        assertThat(jdbc.queryForObject("select deleted_by from blog_post where id = ?", UUID.class, UUID.fromString(id))).isEqualTo(admin.getId());
        List<AuditEvent> events = audit.findByActorIdOrderByOccurredAtDesc(admin.getId());
        assertThat(events).extracting(AuditEvent::getAction).contains("ADMIN_BLOG_POST_CREATED", "ADMIN_BLOG_POST_UPDATED", "ADMIN_BLOG_POST_PUBLISHED", "ADMIN_BLOG_POST_ARCHIVED", "ADMIN_BLOG_POST_DELETED");
        assertThat(events).allSatisfy(event -> assertThat(event.getDetails().toString()).doesNotContain("password", "bodyMarkdown", "storageKey"));
    }

    @Test
    void rejectsDeletedCategoryWhenPublishingAndReturnsOnlyOrderedSafeMediaDtos() throws Exception {
        AppUser admin = actor("media-admin");
        BlogCategory category = categories.saveAndFlush(BlogCategory.create(UUID.randomUUID(), "deleted-category-" + UUID.randomUUID(), 0, Instant.now()));
        MediaAsset inline = asset("inline");
        MediaAsset attachment = asset("attachment");
        String post = createPost(admin, category.getId(), true, List.of(), List.of(new MediaReference(inline.getId(), "INLINE", 0), new MediaReference(attachment.getId(), "ATTACHMENT", 0)));
        category.softDelete(admin, Instant.now());
        categories.saveAndFlush(category);
        mvc.perform(post("/api/v1/admin/blog/posts/{id}/publish", post).param("version", "0")
                        .with(adminUser(admin)).with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isUnprocessableEntity()).andExpect(jsonPath("$.code").value("PUBLISH_VALIDATION_FAILED"));
    }

    private String createPost(AppUser admin, UUID categoryId, boolean seo, List<UUID> tagIds, List<MediaReference> mediaReferences) throws Exception {
        org.springframework.test.web.servlet.ResultActions action = mvc.perform(post("/api/v1/admin/blog/posts").contentType(MediaType.APPLICATION_JSON)
                        .content(postPayload(categoryId, null, seo, tagIds, mediaReferences)).with(adminUser(admin)).with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.hibernateLazyInitializer").doesNotExist());
        if (!tagIds.isEmpty()) action.andExpect(jsonPath("$.tagIds[0]").value(tagIds.getFirst().toString()));
        if (!mediaReferences.isEmpty()) action.andExpect(jsonPath("$.media[0].mediaAssetId").value(mediaReferences.stream().filter(reference -> reference.usage().equals("ATTACHMENT")).min(java.util.Comparator.comparingInt(MediaReference::sortOrder)).orElse(mediaReferences.getFirst()).mediaAssetId().toString()))
                .andExpect(jsonPath("$.storageKey").doesNotExist()).andExpect(jsonPath("$.media[0].storageKey").doesNotExist()).andExpect(jsonPath("$.media[0].originalFilename").doesNotExist());
        String response = action.andReturn().getResponse().getContentAsString();
        return JsonPath.read(response, "$.id");
    }

    private long updatePost(AppUser admin, String id, UUID categoryId, long version, boolean seo) throws Exception {
        String response = mvc.perform(put("/api/v1/admin/blog/posts/{id}", id).contentType(MediaType.APPLICATION_JSON)
                        .content(postPayload(categoryId, version, seo, null, null)).with(adminUser(admin)).with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(response, "$.version")).longValue();
    }

    private static String postPayload(UUID categoryId, Long version, boolean seo, List<UUID> tagIds, List<MediaReference> mediaReferences) {
        String suffix = UUID.randomUUID().toString();
        String versionField = version == null ? "" : ",\"version\":" + version;
        String seoFields = seo ? ",\"seoTitle\":\"SEO\",\"seoDescription\":\"Description\"" : "";
        String tagsField = tagIds == null ? "" : ",\"tagIds\":[" + tagIds.stream().map(value->"\"" + value + "\"").collect(java.util.stream.Collectors.joining(",")) + "]";
        String mediaField = mediaReferences == null ? "" : ",\"media\":[" + mediaReferences.stream().map(reference->"{\"mediaAssetId\":\"" + reference.mediaAssetId() + "\",\"usage\":\"" + reference.usage() + "\",\"sortOrder\":" + reference.sortOrder() + "}").collect(java.util.stream.Collectors.joining(",")) + "]";
        return "{\"categoryId\":\"" + categoryId + "\",\"fa\":{\"title\":\"فا\",\"slug\":\"fa-" + suffix + "\",\"bodyMarkdown\":\"body\"" + seoFields + "},\"en\":{\"title\":\"En\",\"slug\":\"en-" + suffix + "\",\"bodyMarkdown\":\"body\"" + seoFields + "}" + versionField + tagsField + mediaField + "}";
    }

    private MediaAsset asset(String name) { return media.saveAndFlush(MediaAsset.create(UUID.randomUUID(), "storage-" + name + "-" + UUID.randomUUID(), name + ".png", "png", "image/png", 12, "a".repeat(64), 1, 1, Instant.now())); }
    private record MediaReference(UUID mediaAssetId, String usage, int sortOrder) { }
    private AppUser actor(String name) { return users.saveAndFlush(AppUser.create(name + "@example.test", "hash", name, Instant.now())); }
    private static SecurityMockMvcRequestPostProcessors.UserRequestPostProcessor adminUser(AppUser user) { return SecurityMockMvcRequestPostProcessors.user(user.getEmail()).roles("ADMIN"); }
}
