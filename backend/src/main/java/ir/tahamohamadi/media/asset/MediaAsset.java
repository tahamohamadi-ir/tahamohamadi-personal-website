package ir.tahamohamadi.media.asset;

import ir.tahamohamadi.common.persistence.AuditedSoftDeletableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "media_asset")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MediaAsset extends AuditedSoftDeletableEntity {

    @Column(name = "storage_key", nullable = false, length = 255)
    private String storageKey;
    @Column(name = "original_filename", nullable = false, length = 255)
    private String originalFilename;
    @Column(nullable = false, length = 32)
    private String extension;
    @Column(name = "mime_type", nullable = false, length = 255)
    private String mimeType;
    @Column(name = "size_bytes", nullable = false)
    private long sizeBytes;
    @Column(name = "checksum_sha256", nullable = false, length = 64)
    private String checksumSha256;
    private Integer width;
    private Integer height;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MediaAssetStatus status;

    private MediaAsset(UUID id, String storageKey, String originalFilename, String extension, String mimeType,
                       long sizeBytes, String checksumSha256, Integer width, Integer height, Instant createdAt) {
        initialize(id, createdAt);
        this.storageKey = requireNonBlank(storageKey, "storageKey");
        this.originalFilename = requireNonBlank(originalFilename, "originalFilename");
        this.extension = requireNonBlank(extension, "extension");
        this.mimeType = requireNonBlank(mimeType, "mimeType");
        if (sizeBytes <= 0) throw new IllegalArgumentException("sizeBytes must be positive");
        if (width != null && width < 0 || height != null && height < 0) throw new IllegalArgumentException("dimensions must be nonnegative");
        this.sizeBytes = sizeBytes;
        this.checksumSha256 = requireNonBlank(checksumSha256, "checksumSha256");
        this.width = width;
        this.height = height;
        this.status = MediaAssetStatus.ACTIVE;
    }

    public static MediaAsset create(UUID id, String storageKey, String originalFilename, String extension, String mimeType,
                                    long sizeBytes, String checksumSha256, Integer width, Integer height, Instant createdAt) {
        return new MediaAsset(id, storageKey, originalFilename, extension, mimeType, sizeBytes, checksumSha256, width, height, createdAt);
    }

    public static MediaAsset create(String storageKey, String originalFilename, String extension, String mimeType,
                                    long sizeBytes, String checksumSha256, Integer width, Integer height, Instant createdAt) {
        return create(UUID.randomUUID(), storageKey, originalFilename, extension, mimeType, sizeBytes, checksumSha256, width, height, createdAt);
    }

    public void archive() { this.status = MediaAssetStatus.ARCHIVED; }
}
