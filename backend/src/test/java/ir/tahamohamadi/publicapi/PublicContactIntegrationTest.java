package ir.tahamohamadi.publicapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.tahamohamadi.common.domain.LanguageCode;
import ir.tahamohamadi.content.contact.ContactMessage;
import ir.tahamohamadi.content.contact.ContactMessageRepository;
import ir.tahamohamadi.content.contact.ContactMessageStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest(properties = "spring.jpa.hibernate.ddl-auto=validate")
@AutoConfigureMockMvc
@ExtendWith(OutputCaptureExtension.class)
class PublicContactIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("public_contact_test")
            .withUsername("test")
            .withPassword("test");

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired ContactMessageRepository messages;

    @BeforeEach
    void setUp() {
        messages.deleteAllInBatch();
    }

    @Test
    void requiresCsrfForPublicContactSubmission() throws Exception {
        mvc.perform(post("/api/v1/public/contact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayload("csrf@example.com", "csrf protected body")))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        assertThat(messages.count()).isZero();
    }

    @Test
    void rejectsInvalidAndOversizedPayloads() throws Exception {
        mvc.perform(post("/api/v1/public/contact")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name":"",
                                  "email":"not-an-email",
                                  "message":"",
                                  "language":null
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        String oversizedMessage = "x".repeat(10_001);
        mvc.perform(post("/api/v1/public/contact")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayload("oversized@example.com", oversizedMessage)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        assertThat(messages.count()).isZero();
    }

    @Test
    void persistsNewMessageAndReturnsOnlySafeReceiptFields(CapturedOutput output) throws Exception {
        String email = "private@example.com";
        String body = "private contact body";

        String responseBody = mvc.perform(post("/api/v1/public/contact")
                        .with(csrf())
                        .with(request -> {
                            request.setRemoteAddr("198.51.100.10");
                            return request;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayload(email, body)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.status").value("RECEIVED"))
                .andExpect(jsonPath("$.submittedAt").isNotEmpty())
                .andExpect(jsonPath("$.email").doesNotExist())
                .andExpect(jsonPath("$.message").doesNotExist())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode response = objectMapper.readTree(responseBody);
        Set<String> responseFields = new HashSet<>();
        response.fieldNames().forEachRemaining(responseFields::add);
        assertThat(responseFields).containsExactlyInAnyOrder("id", "status", "submittedAt");

        ContactMessage stored = messages.findAll().getFirst();
        assertThat(stored.getStatus()).isEqualTo(ContactMessageStatus.NEW);
        assertThat(stored.getSenderName()).isEqualTo("Taha");
        assertThat(stored.getSenderEmail()).isEqualTo(email);
        assertThat(stored.messageForAdministration()).isEqualTo(body);
        assertThat(stored.getSourceLanguage()).isEqualTo(LanguageCode.en);
        assertThat(stored.toString()).doesNotContain(email, body, "Taha");
        assertThat(output.getOut()).doesNotContain(email, body);
        assertThat(output.getErr()).doesNotContain(email, body);
    }

    @Test
    void allowsRepeatedSubmissionsWithoutApplicationDeduplicationOrFakeRateLimit() throws Exception {
        for (int index = 0; index < 6; index++) {
            mvc.perform(post("/api/v1/public/contact")
                            .with(csrf())
                            .with(request -> {
                                request.setRemoteAddr("198.51.100.20");
                                return request;
                            })
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validPayload("repeat@example.com", "Repeated message")))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value("RECEIVED"));
        }

        assertThat(messages.count()).isEqualTo(6);
        assertThat(messages.findAll())
                .allMatch(message -> message.getStatus() == ContactMessageStatus.NEW)
                .allMatch(message -> message.getSenderEmail().equals("repeat@example.com"));
    }

    private String validPayload(String email, String message) throws Exception {
        return objectMapper.writeValueAsString(new ContactPayload("Taha", email, message, "en"));
    }

    private record ContactPayload(String name, String email, String message, String language) {
    }
}
