package ir.tahamohamadi.media;

import ir.tahamohamadi.media.validation.MediaPolicy;
import ir.tahamohamadi.media.validation.MediaValidationService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MediaValidationUnitTest {

    private final MediaValidationService validation = new MediaValidationService(new MediaPolicy());

    @Test
    void rejectsAPathLikeSubmittedFilename() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "..\\..\\secrets.pdf", "application/pdf", "%PDF-1.4\nbody".getBytes());

        assertThatThrownBy(() -> validation.validate(file))
                .hasMessageContaining("filename");
    }

    @Test
    void rejectsAnExtensionAndContentTypeMismatch() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "document.pdf", "application/pdf", new byte[]{(byte) 0x89, 'P', 'N', 'G'});

        assertThatThrownBy(() -> validation.validate(file))
                .hasMessageContaining("content");
    }
}
