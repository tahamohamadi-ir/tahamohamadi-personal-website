package ir.tahamohamadi.media.api.admin;

import ir.tahamohamadi.media.asset.MediaAssetStatus;
import java.time.Instant;
import java.util.UUID;

public record MediaAssetResponse(UUID id, String originalFilename, String mimeType, long sizeBytes, Integer width,
                                 Integer height, MediaAssetStatus status, String faAlt, String faCaption,
                                 String enAlt, String enCaption, Instant createdAt, long version) { }
