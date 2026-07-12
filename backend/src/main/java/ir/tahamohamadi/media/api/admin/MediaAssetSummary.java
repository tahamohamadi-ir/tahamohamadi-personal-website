package ir.tahamohamadi.media.api.admin;

import ir.tahamohamadi.media.asset.MediaAssetStatus;
import java.time.Instant;
import java.util.UUID;

public record MediaAssetSummary(UUID id, String mimeType, long sizeBytes, MediaAssetStatus status, Instant createdAt, long version) { }
