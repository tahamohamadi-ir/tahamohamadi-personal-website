package ir.tahamohamadi.media.asset;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface MediaAssetRepository extends JpaRepository<MediaAsset, UUID>, JpaSpecificationExecutor<MediaAsset> {
    Optional<MediaAsset> findByStorageKeyAndDeletedAtIsNull(String storageKey);
    Optional<MediaAsset> findByIdAndStatusAndDeletedAtIsNull(UUID id, MediaAssetStatus status);
    Page<MediaAsset> findByDeletedAtIsNullOrderByUpdatedAtDescIdDesc(Pageable pageable);

    @Query(value = """
            select asset.* from media_asset asset
            where asset.deleted_at is null
              and (:query is null or lower(asset.original_filename) like lower(concat('%', :query, '%')))
              and (:mimePrefix is null or asset.mime_type like concat(:mimePrefix, '%'))
              and (:status is null or asset.status = :status)
              and not exists (select 1 from media_usage usage where usage.media_asset_id = asset.id)
            order by asset.updated_at desc, asset.id desc
            """, countQuery = """
            select count(*) from media_asset asset
            where asset.deleted_at is null
              and (:query is null or lower(asset.original_filename) like lower(concat('%', :query, '%')))
              and (:mimePrefix is null or asset.mime_type like concat(:mimePrefix, '%'))
              and (:status is null or asset.status = :status)
              and not exists (select 1 from media_usage usage where usage.media_asset_id = asset.id)
            """, nativeQuery = true)
    Page<MediaAsset> findOrphans(
            @Param("query") String query,
            @Param("mimePrefix") String mimePrefix,
            @Param("status") String status,
            Pageable pageable
    );
}
