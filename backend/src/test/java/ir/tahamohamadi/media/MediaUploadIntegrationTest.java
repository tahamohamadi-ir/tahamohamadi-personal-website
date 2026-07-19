package ir.tahamohamadi.media;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.tahamohamadi.audit.event.AuditEvent;
import ir.tahamohamadi.audit.event.AuditEventRepository;
import ir.tahamohamadi.identity.user.AppUser;
import ir.tahamohamadi.identity.user.AppUserRepository;
import ir.tahamohamadi.media.asset.MediaAssetRepository;
import ir.tahamohamadi.media.asset.MediaAssetStatus;
import ir.tahamohamadi.media.asset.MediaAssetTranslationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest(properties = "taha.media.storage.root=${java.io.tmpdir}/taha-media-test")
@AutoConfigureMockMvc
class MediaUploadIntegrationTest {
    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired AppUserRepository users;
    @Autowired MediaAssetRepository assets;
    @Autowired MediaAssetTranslationRepository translations;
    @Autowired AuditEventRepository audit;

    @BeforeEach
    void setUp() {
        audit.deleteAllInBatch();
        translations.deleteAllInBatch();
        assets.deleteAllInBatch();
        users.deleteAllInBatch();
    }

    @Test
    void securesVersionsAndAttributesAllMediaMutationsWithoutExposingSensitiveMetadata() throws Exception {
        AppUser admin = users.saveAndFlush(AppUser.create(
                "media-admin@example.test",
                "hash",
                "Media Admin",
                Instant.now()
        ));

        mvc.perform(multipart("/api/v1/admin/media")
                        .file(image())
                        .param("faAlt", "alt fa")
                        .param("enAlt", "alt en"))
                .andExpect(status().isForbidden());

        String createdBody = mvc.perform(multipart("/api/v1/admin/media")
                        .file(image())
                        .param("faAlt", "alt fa")
                        .param("enAlt", "alt en")
                        .with(adminUser(admin))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.storageKey").doesNotExist())
                .andExpect(jsonPath("$.originalFilename").value("photo.png"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode created = mapper.readTree(createdBody);
        UUID id = UUID.fromString(created.path("id").asText());
        long initialVersion = created.path("version").asLong();

        String updatedBody = mvc.perform(put("/api/v1/admin/media/{id}/metadata", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "version": %d,
                                  "faAlt": "updated fa",
                                  "faCaption": "caption fa",
                                  "enAlt": "updated en",
                                  "enCaption": "caption en"
                                }
                                """.formatted(initialVersion))
                        .with(adminUser(admin))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.storageKey").doesNotExist())
                .andReturn()
                .getResponse()
                .getContentAsString();

        long updatedVersion = mapper.readTree(updatedBody).path("version").asLong();
        assertThat(updatedVersion).isGreaterThan(initialVersion);

        mvc.perform(delete("/api/v1/admin/media/{id}", id)
                        .param("version", Long.toString(initialVersion))
                        .with(adminUser(admin))
                        .with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("OPTIMISTIC_LOCK_CONFLICT"));

        mvc.perform(delete("/api/v1/admin/media/{id}", id)
                        .param("version", Long.toString(updatedVersion))
                        .with(adminUser(admin))
                        .with(csrf()))
                .andExpect(status().isNoContent());

        assertThat(assets.findById(id).orElseThrow().getStatus())
                .isEqualTo(MediaAssetStatus.ARCHIVED);

        List<AuditEvent> events = audit.findByActorIdOrderByOccurredAtDesc(admin.getId());
        assertThat(events)
                .extracting(AuditEvent::getAction)
                .containsExactlyInAnyOrder(
                        "ADMIN_MEDIA_UPLOADED",
                        "ADMIN_MEDIA_METADATA_UPDATED",
                        "ADMIN_MEDIA_ARCHIVED"
                );
        assertThat(events).allSatisfy(event -> {
            assertThat(event.getActor().getId()).isEqualTo(admin.getId());
            assertThat(event.getTargetId()).isEqualTo(id);
            assertThat(event.getDetails().toString())
                    .doesNotContain("photo.png", "updated fa", "updated en", "caption");
        });
    }

    private static MockMultipartFile image() throws Exception {
        return new MockMultipartFile("file", "photo.png", "image/png", png());
    }

    private static byte[] png() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB), "png", output);
        return output.toByteArray();
    }

    private static SecurityMockMvcRequestPostProcessors.UserRequestPostProcessor adminUser(AppUser user) {
        return SecurityMockMvcRequestPostProcessors.user(user.getEmail()).roles("ADMIN");
    }
}
