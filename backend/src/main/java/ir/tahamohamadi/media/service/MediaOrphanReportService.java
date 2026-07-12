package ir.tahamohamadi.media.service;

import ir.tahamohamadi.media.asset.MediaAssetRepository;
import ir.tahamohamadi.media.api.admin.MediaOrphanResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import java.util.List;

@Service @ConditionalOnExpression("!'${spring.autoconfigure.exclude:}'.contains('DataSourceAutoConfiguration')")
public class MediaOrphanReportService {
    private final MediaAssetRepository assets; private final MediaReferenceService references;
    public MediaOrphanReportService(MediaAssetRepository assets, MediaReferenceService references) { this.assets = assets; this.references = references; }
    public List<MediaOrphanResponse> findOrphans() { return assets.findByDeletedAtIsNullOrderByUpdatedAtDescIdDesc(PageRequest.of(0, 100)).stream().filter(a -> !references.isReferenced(a.getId())).map(a -> new MediaOrphanResponse(a.getId())).toList(); }
}
