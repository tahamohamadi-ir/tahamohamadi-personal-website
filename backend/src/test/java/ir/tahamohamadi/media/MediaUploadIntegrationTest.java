package ir.tahamohamadi.media;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest(properties = "taha.media.storage.root=${java.io.tmpdir}/taha-media-test")
@AutoConfigureMockMvc
class MediaUploadIntegrationTest {
    @Container @ServiceConnection static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine");
    @Autowired MockMvc mvc;

    @Test
    void onlyAnAdministratorWithCsrfCanUploadAndResponseDoesNotExposeStorageKey() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file","photo.png","image/png",png());
        mvc.perform(multipart("/api/v1/admin/media").file(file).param("faAlt","alt fa").param("enAlt","alt en"))
                .andExpect(status().isForbidden());
        mvc.perform(multipart("/api/v1/admin/media").file(file).param("faAlt","alt fa").param("enAlt","alt en")
                        .with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.storageKey").doesNotExist())
                .andExpect(jsonPath("$.originalFilename").value("photo.png"));
    }
    private static byte[] png() throws Exception { ByteArrayOutputStream out=new ByteArrayOutputStream(); ImageIO.write(new BufferedImage(1,1,BufferedImage.TYPE_INT_RGB),"png",out); return out.toByteArray(); }
}
