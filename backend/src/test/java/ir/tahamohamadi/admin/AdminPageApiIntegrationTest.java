package ir.tahamohamadi.admin;

import ir.tahamohamadi.identity.user.AppUser;
import ir.tahamohamadi.identity.user.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class AdminPageApiIntegrationTest {
    @Container @ServiceConnection static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine");
    @Autowired MockMvc mvc;
    @Autowired AppUserRepository users;

    @Test
    void requiresAdminAndCreatesOnlyDtoResponse() throws Exception {
        AppUser admin = users.saveAndFlush(AppUser.create("page-admin-" + UUID.randomUUID() + "@example.test", "hash", "Page admin", Instant.now()));
        String body = """
                {"pageKey":"about","fa":{"title":"درباره","slug":"about-fa","seoTitle":"درباره","seoDescription":"معرفی"},"en":{"title":"About","slug":"about","seoTitle":"About","seoDescription":"Introduction"}}
                """;
        mvc.perform(post("/api/v1/admin/pages").contentType(APPLICATION_JSON).content(body)
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/v1/admin/pages").contentType(APPLICATION_JSON).content(body)
                        .with(SecurityMockMvcRequestPostProcessors.user(admin.getEmail()).roles("ADMIN"))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.id").isString())
                .andExpect(jsonPath("$.fa.title").value("درباره"))
                .andExpect(jsonPath("$.version").value(0))
                .andExpect(jsonPath("$.createdAt").doesNotExist())
                .andExpect(jsonPath("$.hibernateLazyInitializer").doesNotExist());
    }
}
