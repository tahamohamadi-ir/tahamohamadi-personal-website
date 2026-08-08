package ir.tahamohamadi.media.asset;

import ir.tahamohamadi.common.domain.LanguageCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MediaAssetTranslationRepository extends JpaRepository<MediaAssetTranslation, UUID> {
    Optional<MediaAssetTranslation> findByMediaAssetIdAndLanguageCodeAndDeletedAtIsNull(UUID mediaAssetId, LanguageCode languageCode);
    List<MediaAssetTranslation> findByMediaAssetIdInAndDeletedAtIsNull(Collection<UUID> mediaAssetIds);
}
