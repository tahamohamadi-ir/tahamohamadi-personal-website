package ir.tahamohamadi.media.asset;

import ir.tahamohamadi.common.domain.LanguageCode;
import ir.tahamohamadi.common.persistence.AuditedSoftDeletableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "media_asset_translation")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MediaAssetTranslation extends AuditedSoftDeletableEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "media_asset_id", nullable = false)
    private MediaAsset mediaAsset;
    @Enumerated(EnumType.STRING)
    @Column(name = "language_code", nullable = false, length = 2)
    private LanguageCode languageCode;
    @Column(name = "alt_text", nullable = false, length = 500)
    private String altText;
    @Column(columnDefinition = "text")
    private String caption;

    private MediaAssetTranslation(UUID id, MediaAsset mediaAsset, LanguageCode languageCode, String altText, String caption, Instant createdAt) {
        initialize(id, createdAt);
        this.mediaAsset = Objects.requireNonNull(mediaAsset, "mediaAsset must not be null");
        this.languageCode = Objects.requireNonNull(languageCode, "languageCode must not be null");
        this.altText = Objects.requireNonNull(altText, "altText must not be null");
        this.caption = caption;
    }
    public static MediaAssetTranslation create(UUID id, MediaAsset asset, LanguageCode language, String altText, String caption, Instant createdAt) {
        return new MediaAssetTranslation(id, asset, language, altText, caption, createdAt);
    }
}
