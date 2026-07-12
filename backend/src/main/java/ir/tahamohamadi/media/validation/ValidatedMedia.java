package ir.tahamohamadi.media.validation;

public record ValidatedMedia(String originalFilename, String mimeType, String extension, long sizeBytes,
                             Integer width, Integer height) { }
