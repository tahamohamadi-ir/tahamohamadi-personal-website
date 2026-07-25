package ir.tahamohamadi.admin;

import ir.tahamohamadi.audit.event.AuditEventRepository;
import ir.tahamohamadi.identity.user.AppUser;
import ir.tahamohamadi.identity.user.AppUserRepository;
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
class AdminNavigationIntegrationTest {
    @Container @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired AppUserRepository users;
    @Autowired AuditEventRepository audit;

    @BeforeEach
    void setUp() {
        audit.deleteAllInBatch();
        jdbc.update("delete from site_navigation_item_translation");
        jdbc.update("delete from site_navigation_item");
        users.deleteAllInBatch();
    }

    @Test
    void requiresAdminAndSavesOnlyApprovedBilingualTargets() throws Exception {
        AppUser admin = users.saveAndFlush(AppUser.create("navigation-" + UUID.randomUUID() + "@example.test", "hash", "Navigation Admin", Instant.now()));

        mvc.perform(put("/api/v1/admin/navigation").contentType(MediaType.APPLICATION_JSON).content(payload("javascript:alert(1)")).with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isUnauthorized());
        mvc.perform(put("/api/v1/admin/navigation").contentType(MediaType.APPLICATION_JSON).content(payload("javascript:alert(1)")).with(adminUser(admin)).with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isBadRequest());
        mvc.perform(put("/api/v1/admin/navigation").contentType(MediaType.APPLICATION_JSON).content(payload("/{lang}/about")).with(adminUser(admin)).with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].targetPath").value("/{lang}/about"))
                .andExpect(jsonPath("$.items[0].fa.label").value("\u062f\u0631\u0628\u0627\u0631\u0647"))
                .andExpect(jsonPath("$.items[0].en.label").value("About"));
    }

    private static String payload(String targetPath) {
        return """
                {"items":[{"key":"about","targetPath":"%s","externalTarget":false,"active":true,"fa":{"label":"\u062f\u0631\u0628\u0627\u0631\u0647"},"en":{"label":"About"}}]}
                """.formatted(targetPath.replace("\\", "\\\\").replace("\"", "\\\""));
    }

    private static SecurityMockMvcRequestPostProcessors.UserRequestPostProcessor adminUser(AppUser user) {
        return SecurityMockMvcRequestPostProcessors.user(user.getEmail()).roles("ADMIN");
    }
}
