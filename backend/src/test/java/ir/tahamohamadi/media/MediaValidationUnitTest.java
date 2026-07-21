package ir.tahamohamadi.media;

import ir.tahamohamadi.media.validation.MediaPolicy;
import ir.tahamohamadi.media.validation.MediaValidationService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
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

    @Test
    void rejectsAnImageAboveTheEstablishedBusinessMaximum() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "large.png", "image/png", new byte[(int) MediaPolicy.MAX_IMAGE_BYTES + 1]);

        assertThatThrownBy(() -> validation.validate(file))
                .hasMessageContaining("allowed size");
    }

    @Test
    void acceptsAPdfBelowTheEstablishedBusinessMaximum() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "document.pdf", "application/pdf", "%PDF-1.4\nbody".getBytes());

        assertThat(validation.validate(file).mimeType()).isEqualTo("application/pdf");
    }

    @Test
    void rejectsAPdfAboveTheEstablishedBusinessMaximum() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "large.pdf", "application/pdf", new byte[(int) MediaPolicy.MAX_PDF_BYTES + 1]);

        assertThatThrownBy(() -> validation.validate(file))
                .hasMessageContaining("allowed size");
    }
}
