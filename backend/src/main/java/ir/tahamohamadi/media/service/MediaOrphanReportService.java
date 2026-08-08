package ir.tahamohamadi.media.service;

import ir.tahamohamadi.media.api.admin.MediaOrphanResponse;
import ir.tahamohamadi.media.api.admin.MediaUsageResponse;
import ir.tahamohamadi.media.asset.MediaAsset;
import ir.tahamohamadi.media.asset.MediaAssetRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
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

    public Page<MediaOrphanResponse> findOrphans(
            Pageable pageable,
            String query,
            String mimePrefix,
            ir.tahamohamadi.media.asset.MediaAssetStatus status
    ) {
        references.refreshUsageIndex();
        return assets.findOrphans(query, mimePrefix, status == null ? null : status.name(), pageable)
                .map(asset -> new MediaOrphanResponse(
                        asset.getId(), asset.getOriginalFilename(), asset.getMimeType(), asset.getStatus().name()
                ));
    }

    public List<MediaUsageResponse> usages(UUID id) {
        return references.usages(id);
    }
}
