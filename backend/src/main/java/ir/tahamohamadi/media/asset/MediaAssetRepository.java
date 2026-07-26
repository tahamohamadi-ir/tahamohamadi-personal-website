package ir.tahamohamadi.media.asset;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface MediaAssetRepository extends JpaRepository<MediaAsset, UUID>, JpaSpecificationExecutor<MediaAsset> {
    Optional<MediaAsset> findByStorageKeyAndDeletedAtIsNull(String storageKey);
    Optional<MediaAsset> findByIdAndStatusAndDeletedAtIsNull(UUID id, MediaAssetStatus status);
    Page<MediaAsset> findByDeletedAtIsNullOrderByUpdatedAtDescIdDesc(Pageable pageable);
}
