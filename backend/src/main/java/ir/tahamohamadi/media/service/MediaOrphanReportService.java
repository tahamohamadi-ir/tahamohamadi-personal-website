package ir.tahamohamadi.media.service;

import ir.tahamohamadi.media.api.admin.MediaOrphanResponse;
import ir.tahamohamadi.media.asset.MediaAsset;
import ir.tahamohamadi.media.asset.MediaAssetRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@ConditionalOnExpression("!'${spring.autoconfigure.exclude:}'.contains('DataSourceAutoConfiguration')")
public class MediaOrphanReportService {
    private final MediaAssetRepository assets;
    private final MediaReferenceService references;

    public MediaOrphanReportService(
            MediaAssetRepository assets,
            MediaReferenceService references
    ) {
        this.assets = assets;
        this.references = references;
    }

    public List<MediaOrphanResponse> findOrphans() {
        List<MediaAsset> candidates = assets
                .findByDeletedAtIsNullOrderByUpdatedAtDescIdDesc(PageRequest.of(0, 100))
                .getContent();
        Set<UUID> referencedIds = references.referencedIds(
                candidates.stream().map(MediaAsset::getId).toList()
        );

        return candidates.stream()
                .filter(asset -> !referencedIds.contains(asset.getId()))
                .map(asset -> new MediaOrphanResponse(asset.getId()))
                .toList();
    }
}
