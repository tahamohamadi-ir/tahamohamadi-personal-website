package ir.tahamohamadi.admin;

import ir.tahamohamadi.audit.event.AuditEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.tahamohamadi.content.page.ContentPage;
import ir.tahamohamadi.content.page.ContentPageRepository;
import ir.tahamohamadi.identity.user.AppUser;
import ir.tahamohamadi.identity.user.AppUserRepository;
import ir.tahamohamadi.media.asset.MediaAsset;
import ir.tahamohamadi.media.asset.MediaAssetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class AdminPageBlockIntegrationTest {
    @Container @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired JdbcTemplate jdbc;
    @Autowired AppUserRepository users;
    @Autowired ContentPageRepository pages;
    @Autowired MediaAssetRepository media;
    @Autowired AuditEventRepository audit;

    @BeforeEach
    void setUp() {
        audit.deleteAllInBatch();
        jdbc.update("delete from content_page_section");
        pages.deleteAllInBatch();
        users.deleteAllInBatch();
    }

    @Test
    void requiresAdminAndPersistsTypedLocalizedBlocks() throws Exception {
        AppUser admin = users.saveAndFlush(AppUser.create("composer-" + UUID.randomUUID() + "@example.test", "hash", "Composer Admin", Instant.now()));
        ContentPage page = pages.saveAndFlush(ContentPage.create(UUID.randomUUID(), "composer-" + UUID.randomUUID(), Instant.now()));

        mvc.perform(put("/api/v1/admin/pages/{pageId}/blocks", page.getId()).contentType(MediaType.APPLICATION_JSON).content(payload()).with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isUnauthorized());
        mvc.perform(put("/api/v1/admin/pages/{pageId}/blocks", page.getId()).contentType(MediaType.APPLICATION_JSON).content(payload()).with(adminUser(admin)).with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").value(1))
                .andExpect(jsonPath("$.blocks[0].type").value("RICH_TEXT"))
                .andExpect(jsonPath("$.blocks[0].fa.title").value("\u0639\u0646\u0648\u0627\u0646"))
                .andExpect(jsonPath("$.blocks[0].en.title").value("Title"));
    }

    @Test
    void storesAndReadsAnAdditiveSectionCompositionWithoutChangingTheFlatBlocksContract() throws Exception {
        AppUser admin = users.saveAndFlush(AppUser.create("composition-" + UUID.randomUUID() + "@example.test", "hash", "Composition Admin", Instant.now()));
        ContentPage page = pages.saveAndFlush(ContentPage.create(UUID.randomUUID(), "composition-" + UUID.randomUUID(), Instant.now()));

        mvc.perform(put("/api/v1/admin/pages/{pageId}/blocks/composition", page.getId())
                        .contentType(MediaType.APPLICATION_JSON).content(compositionPayload())
                        .with(adminUser(admin)).with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").value(1))
                .andExpect(jsonPath("$.sections.length()").value(2))
                .andExpect(jsonPath("$.sections[0].layout").value("SINGLE_COLUMN"))
                .andExpect(jsonPath("$.sections[1].blocks[0].type").value("CALL_TO_ACTION"));

        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/admin/pages/{pageId}/blocks/composition", page.getId())
                        .with(adminUser(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sections.length()").value(2))
                .andExpect(jsonPath("$.sections[0].blocks[0].en.title").value("First"));
    }

    @Test
    void requiresLocalizedAltOrAnExplicitDecorativeDecisionForMediaBlocks() throws Exception {
        AppUser admin = users.saveAndFlush(AppUser.create("media-alt-" + UUID.randomUUID() + "@example.test", "hash", "Media Alt Admin", Instant.now()));
        ContentPage page = pages.saveAndFlush(ContentPage.create(UUID.randomUUID(), "media-alt-" + UUID.randomUUID(), Instant.now()));
        MediaAsset image = media.saveAndFlush(MediaAsset.create(UUID.randomUUID(), "media-alt-" + UUID.randomUUID(), "image.png", "png", "image/png", 1, "a".repeat(64), 1, 1, Instant.now()));

        mvc.perform(put("/api/v1/admin/pages/{pageId}/blocks", page.getId())
                        .contentType(MediaType.APPLICATION_JSON).content(mediaPayload(image.getId(), false, "", ""))
                        .with(adminUser(admin)).with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isBadRequest());

        mvc.perform(put("/api/v1/admin/pages/{pageId}/blocks", page.getId())
                        .contentType(MediaType.APPLICATION_JSON).content(mediaPayload(image.getId(), true, "", ""))
                        .with(adminUser(admin)).with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.blocks[0].settingsJson").value(org.hamcrest.Matchers.containsString("decorative")));
    }

    @Test
    void servesDraftPreviewOnlyWithShortLivedTokenAndNoStoreNoIndexHeaders() throws Exception {
        AppUser admin = users.saveAndFlush(AppUser.create("preview-" + UUID.randomUUID() + "@example.test", "hash", "Preview Admin", Instant.now()));
        ContentPage page = pages.saveAndFlush(ContentPage.create(UUID.randomUUID(), "preview-" + UUID.randomUUID(), Instant.now()));
        mvc.perform(put("/api/v1/admin/pages/{pageId}/blocks", page.getId()).contentType(MediaType.APPLICATION_JSON).content(payload()).with(adminUser(admin)).with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk());
        String issued = mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/admin/pages/{pageId}/preview-token", page.getId()).with(adminUser(admin)).with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String token = mapper.readTree(issued).path("token").asText();

        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/preview/pages/{pageId}", page.getId()).param("lang", "en"))
                .andExpect(status().isBadRequest());
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/preview/pages/{pageId}", page.getId()).param("lang", "en").param("token", "not-a-valid-preview-token"))
                .andExpect(status().isNotFound());
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/preview/pages/{pageId}", page.getId()).param("lang", "en").param("token", token))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header().string("Cache-Control", org.hamcrest.Matchers.containsString("no-store")))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header().string("X-Robots-Tag", "noindex, nofollow"))
                .andExpect(jsonPath("$.blocks[0].title").value("Title"));

        jdbc.update("update content_page_section set is_enabled=false where content_page_id=?", page.getId());
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/preview/pages/{pageId}", page.getId()).param("lang", "en").param("token", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.blocks[0].enabled").value(false));

        jdbc.update("update content_page set deleted_at=current_timestamp where id=?", page.getId());
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/preview/pages/{pageId}", page.getId()).param("lang", "en").param("token", token))
                .andExpect(status().isNotFound());
    }

    private static String payload() {
        return """
                {"version":0,"blocks":[{"type":"RICH_TEXT","enabled":true,"fa":{"title":"\u0639\u0646\u0648\u0627\u0646","bodyMarkdown":"\u0645\u062a\u0646 \u0627\u0645\u0646"},"en":{"title":"Title","bodyMarkdown":"Safe text"}}]}
                """;
    }

    private static String compositionPayload() {
        return """
                {"version":0,"sections":[
                  {"type":"STANDARD","layout":"SINGLE_COLUMN","enabled":true,"blocks":[{"type":"RICH_TEXT","enabled":true,"fa":{"title":"اول"},"en":{"title":"First"}}]},
                  {"type":"STANDARD","layout":"SINGLE_COLUMN","enabled":true,"blocks":[{"type":"CALL_TO_ACTION","enabled":true,"fa":{"title":"دوم","actionPath":"/fa/contact"},"en":{"title":"Second","actionPath":"/en/contact"}}]}
                ]}
                """;
    }

    private static String mediaPayload(UUID mediaId, boolean decorative, String faAlt, String enAlt) {
        return "{\"version\":0,\"blocks\":[{\"type\":\"MEDIA\",\"enabled\":true,\"settingsJson\":\"{\\\"mediaId\\\":\\\"" + mediaId + "\\\",\\\"decorative\\\":" + decorative + "}\",\"fa\":{\"alt\":\"" + faAlt + "\"},\"en\":{\"alt\":\"" + enAlt + "\"}}]}";
    }

    private static SecurityMockMvcRequestPostProcessors.UserRequestPostProcessor adminUser(AppUser user) {
        return SecurityMockMvcRequestPostProcessors.user(user.getEmail()).roles("ADMIN");
    }
}
