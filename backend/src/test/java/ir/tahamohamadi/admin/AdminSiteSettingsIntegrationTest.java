package ir.tahamohamadi.admin;

import ir.tahamohamadi.audit.event.AuditEventRepository;
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
class AdminSiteSettingsIntegrationTest {
    @Container @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired AppUserRepository users;
    @Autowired MediaAssetRepository mediaAssets;
    @Autowired AuditEventRepository audit;

    @BeforeEach
    void setUp() {
        audit.deleteAllInBatch();
        jdbc.update("delete from site_setting_translation");
        jdbc.update("delete from site_setting");
        mediaAssets.deleteAllInBatch();
        users.deleteAllInBatch();
    }

    @Test
    void requiresAdminAndOnlyAcceptsActiveMediaLibraryAssets() throws Exception {
        AppUser admin = users.saveAndFlush(AppUser.create("site-settings-" + UUID.randomUUID() + "@example.test", "hash", "Site Settings Admin", Instant.now()));
        MediaAsset activeAsset = mediaAssets.saveAndFlush(MediaAsset.create("settings/" + UUID.randomUUID() + ".png", "identity.png", "png", "image/png", 128, "a".repeat(64), 1, 1, Instant.now()));

        mvc.perform(put("/api/v1/admin/site-settings").contentType(MediaType.APPLICATION_JSON).content(payload(activeAsset.getId())).with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isUnauthorized());
        mvc.perform(put("/api/v1/admin/site-settings").contentType(MediaType.APPLICATION_JSON).content(payload(UUID.randomUUID())).with(adminUser(admin)).with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isBadRequest());
        mvc.perform(put("/api/v1/admin/site-settings").contentType(MediaType.APPLICATION_JSON).content(payload(activeAsset.getId())).with(adminUser(admin)).with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.logoMediaId").value(activeAsset.getId().toString()))
                .andExpect(jsonPath("$.ogMediaId").value(activeAsset.getId().toString()))
                .andExpect(jsonPath("$.translations.fa.brandName").value("\u0646\u0627\u0645"))
                .andExpect(jsonPath("$.translations.en.footerStatement").value("Structured footer copy"));
    }

    private static String payload(UUID mediaId) {
        return """
                {"version":0,"fa":{"brandName":"\u0646\u0627\u0645","tagline":"Tagline","footerStatement":"Footer statement","footerAvailability":"Available","footerRights":"Rights"},"en":{"brandName":"Name","tagline":"Introduction","footerStatement":"Structured footer copy","footerAvailability":"Available","footerRights":"Rights"},"logoMediaId":"%s","ogMediaId":"%s","themePreset":"EDITORIAL_NAVY","layoutDensity":"COMFORTABLE"}
                """.formatted(mediaId, mediaId);
    }

    private static SecurityMockMvcRequestPostProcessors.UserRequestPostProcessor adminUser(AppUser user) {
        return SecurityMockMvcRequestPostProcessors.user(user.getEmail()).roles("ADMIN");
    }
}
