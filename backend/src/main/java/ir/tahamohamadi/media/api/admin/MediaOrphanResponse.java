package ir.tahamohamadi.media.api.admin;

import java.util.UUID;
public record MediaOrphanResponse(UUID id, String originalFilename, String mimeType, String status) { }
