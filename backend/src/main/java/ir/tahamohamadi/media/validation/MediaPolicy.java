package ir.tahamohamadi.media.validation;

import java.util.Map;

/** Central, deliberately small MVP allow-list. */
public final class MediaPolicy {
    public static final long MAX_IMAGE_BYTES = 10L * 1024 * 1024;
    public static final long MAX_PDF_BYTES = 20L * 1024 * 1024;
    private static final Map<String, String> EXTENSIONS = Map.of(
            "image/jpeg", "jpg", "image/png", "png", "image/webp", "webp", "application/pdf", "pdf");

    public boolean supports(String mimeType) { return EXTENSIONS.containsKey(mimeType); }
    public String canonicalExtension(String mimeType) { return EXTENSIONS.get(mimeType); }
    public long maximumSize(String mimeType) { return mimeType.startsWith("image/") ? MAX_IMAGE_BYTES : MAX_PDF_BYTES; }
}
