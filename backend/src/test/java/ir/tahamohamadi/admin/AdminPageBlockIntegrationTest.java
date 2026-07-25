package ir.tahamohamadi.admin;

import ir.tahamohamadi.audit.event.AuditEventRepository;
import ir.tahamohamadi.content.page.ContentPage;
import ir.tahamohamadi.content.page.ContentPageRepository;
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
class AdminPageBlockIntegrationTest {
    @Container @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired AppUserRepository users;
    @Autowired ContentPageRepository pages;
    @Autowired AuditEventRepository audit;

    @BeforeEach
    void setUp() {
        audit.deleteAllInBatch();
        jdbc.update("delete from content_page_block_translation");
        jdbc.update("delete from content_page_block");
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

    private static String payload() {
        return """
                {"version":0,"blocks":[{"type":"RICH_TEXT","enabled":true,"fa":{"title":"\u0639\u0646\u0648\u0627\u0646","bodyMarkdown":"\u0645\u062a\u0646 \u0627\u0645\u0646"},"en":{"title":"Title","bodyMarkdown":"Safe text"}}]}
                """;
    }

    private static SecurityMockMvcRequestPostProcessors.UserRequestPostProcessor adminUser(AppUser user) {
        return SecurityMockMvcRequestPostProcessors.user(user.getEmail()).roles("ADMIN");
    }
}
