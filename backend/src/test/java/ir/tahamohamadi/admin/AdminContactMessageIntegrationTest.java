package ir.tahamohamadi.admin;

import ir.tahamohamadi.audit.event.AuditEvent;
import ir.tahamohamadi.audit.event.AuditEventRepository;
import ir.tahamohamadi.common.domain.LanguageCode;
import ir.tahamohamadi.content.contact.ContactMessage;
import ir.tahamohamadi.content.contact.ContactMessageRepository;
import ir.tahamohamadi.content.contact.ContactMessageStatus;
import ir.tahamohamadi.identity.user.AppUser;
import ir.tahamohamadi.identity.user.AppUserRepository;
import org.junit.jupiter.api.BeforeEach;
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
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class AdminContactMessageIntegrationTest {
    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired MockMvc mvc;
    @Autowired AppUserRepository users;
    @Autowired ContactMessageRepository messages;
    @Autowired AuditEventRepository audit;

    @BeforeEach
    void setUp() {
        audit.deleteAllInBatch();
        messages.deleteAllInBatch();
        users.deleteAllInBatch();
    }

    @Test
    void enforcesSecurityAuditsTransitionsAndDoesNotLeakContactPiiIntoAuditDetails() throws Exception {
        AppUser admin = users.saveAndFlush(AppUser.create(
                "contact-admin@example.test",
                "hash",
                "Contact Admin",
                Instant.now()
        ));
        ContactMessage message = messages.saveAndFlush(ContactMessage.submit(
                UUID.randomUUID(),
                "Private Sender",
                "private@example.test",
                "private contact body",
                LanguageCode.en,
                Instant.now()
        ));

        mvc.perform(post("/api/v1/admin/contact-messages/{id}/read", message.getId())
                        .with(csrf()))
                .andExpect(status().isUnauthorized());

        mvc.perform(post("/api/v1/admin/contact-messages/{id}/read", message.getId())
                        .with(SecurityMockMvcRequestPostProcessors.user("reader@example.test").roles("USER"))
                        .with(csrf()))
                .andExpect(status().isForbidden());

        mvc.perform(post("/api/v1/admin/contact-messages/{id}/read", message.getId())
                        .with(adminUser(admin)))
                .andExpect(status().isForbidden());

        mvc.perform(post("/api/v1/admin/contact-messages/{id}/read", message.getId())
                        .with(adminUser(admin))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(message.getId().toString()))
                .andExpect(jsonPath("$.status").value("READ"));

        mvc.perform(post("/api/v1/admin/contact-messages/{id}/archive", message.getId())
                        .with(adminUser(admin))
                        .with(csrf()))
                .andExpect(status().isNoContent());

        mvc.perform(post("/api/v1/admin/contact-messages/{id}/read", message.getId())
                        .with(adminUser(admin))
                        .with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("STATE_CONFLICT"));

        ContactMessage stored = messages.findById(message.getId()).orElseThrow();
        assertThat(stored.getStatus()).isEqualTo(ContactMessageStatus.ARCHIVED);

        List<AuditEvent> events = audit.findByActorIdOrderByOccurredAtDesc(admin.getId());
        assertThat(events)
                .extracting(AuditEvent::getAction)
                .containsExactlyInAnyOrder(
                        "ADMIN_CONTACT_MESSAGE_READ",
                        "ADMIN_CONTACT_MESSAGE_ARCHIVED"
                );
        assertThat(events).allSatisfy(event -> {
            assertThat(event.getActor().getId()).isEqualTo(admin.getId());
            assertThat(event.getTargetId()).isEqualTo(message.getId());
            assertThat(event.getDetails().toString())
                    .doesNotContain(
                            message.getSenderName(),
                            message.getSenderEmail(),
                            message.messageForAdministration()
                    );
        });
    }

    private static SecurityMockMvcRequestPostProcessors.UserRequestPostProcessor adminUser(AppUser user) {
        return SecurityMockMvcRequestPostProcessors.user(user.getEmail()).roles("ADMIN");
    }
}
