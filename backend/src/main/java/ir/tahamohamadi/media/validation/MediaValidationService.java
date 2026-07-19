package ir.tahamohamadi.media.validation;

import ir.tahamohamadi.media.api.MediaUploadException;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

public final class MediaValidationService {
    private final MediaPolicy policy;

    public MediaValidationService(MediaPolicy policy) { this.policy = policy; }

    public ValidatedMedia validate(MultipartFile file) {
        if (file == null || file.isEmpty() || file.getSize() <= 0) throw MediaUploadException.invalid("File must not be empty");
        String filename = sanitizeFilename(file.getOriginalFilename());
        String declaredMime = normalizeMime(file.getContentType());
        if (!policy.supports(declaredMime)) throw new MediaUploadException("Unsupported media type");
        if (file.getSize() > policy.maximumSize(declaredMime)) throw MediaUploadException.tooLarge("File exceeds allowed size");
        String submittedExtension = extension(filename);
        if (!submittedExtension.equals(policy.canonicalExtension(declaredMime))
                && !(declaredMime.equals("image/jpeg") && submittedExtension.equals("jpeg"))) {
            throw new MediaUploadException("Filename extension does not match content type");
        }
        try {
            String detectedMime = detect(readPrefix(file, 32));
            if (!declaredMime.equals(detectedMime)) throw new MediaUploadException("File content does not match declared type");
            Integer width = null;
            Integer height = null;
            if (detectedMime.equals("image/jpeg") || detectedMime.equals("image/png")) {
                var image = readImage(file);
                if (image == null) throw new MediaUploadException("Malformed image content");
                width = image.getWidth(); height = image.getHeight();
            }
            return new ValidatedMedia(filename, detectedMime, policy.canonicalExtension(detectedMime), file.getSize(), width, height);
        } catch (IOException exception) {
            throw new MediaUploadException("Unable to validate uploaded content", exception);
        }
    }

    private static String normalizeMime(String value) {
        if (value == null) throw MediaUploadException.invalid("Missing content type");
        return value.toLowerCase(Locale.ROOT).trim();
    }

    private static String sanitizeFilename(String value) {
        if (value == null || value.isBlank() || value.length() > 255 || value.indexOf('\u0000') >= 0
                || value.contains("..") || value.contains("/") || value.contains("\\") || value.matches("^[A-Za-z]:.*")
                || value.startsWith("~")) throw MediaUploadException.invalid("Invalid filename");
        for (int i = 0; i < value.length(); i++) if (Character.isISOControl(value.charAt(i))) throw MediaUploadException.invalid("Invalid filename");
        return value.trim().replaceAll("[^A-Za-z0-9._ -]", "_");
    }

    private static String extension(String filename) {
        int separator = filename.lastIndexOf('.');
        if (separator <= 0 || separator == filename.length() - 1) throw MediaUploadException.invalid("Filename has no valid extension");
        return filename.substring(separator + 1).toLowerCase(Locale.ROOT);
    }

    private static String detect(byte[] bytes) {
        if (bytes.length >= 8 && bytes[0] == (byte) 0x89 && bytes[1] == 'P' && bytes[2] == 'N' && bytes[3] == 'G') return "image/png";
        if (bytes.length >= 3 && bytes[0] == (byte) 0xff && bytes[1] == (byte) 0xd8 && bytes[2] == (byte) 0xff) return "image/jpeg";
        if (bytes.length >= 12 && bytes[0] == 'R' && bytes[1] == 'I' && bytes[2] == 'F' && bytes[3] == 'F'
                && bytes[8] == 'W' && bytes[9] == 'E' && bytes[10] == 'B' && bytes[11] == 'P') return "image/webp";
        if (bytes.length >= 5 && bytes[0] == '%' && bytes[1] == 'P' && bytes[2] == 'D' && bytes[3] == 'F' && bytes[4] == '-') return "application/pdf";
        throw new MediaUploadException("Unsupported or malformed file content");
    }

    private static byte[] readPrefix(MultipartFile file, int limit) throws IOException {
        try (InputStream input = file.getInputStream()) { return input.readNBytes(limit); }
    }

    private static java.awt.image.BufferedImage readImage(MultipartFile file) throws IOException {
        try (InputStream input = file.getInputStream()) { return ImageIO.read(input); }
    }
}
