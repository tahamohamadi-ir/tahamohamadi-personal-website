package ir.tahamohamadi.portfolio.project;

import ir.tahamohamadi.media.asset.MediaAsset;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.Objects;

@Entity
@Table(name = "portfolio_project_media")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PortfolioProjectMedia {
    @EmbeddedId private PortfolioProjectMediaId id;
    @ManyToOne(fetch = FetchType.LAZY) @MapsId("portfolioProjectId") @JoinColumn(name = "portfolio_project_id") private PortfolioProject project;
    @ManyToOne(fetch = FetchType.LAZY) @MapsId("mediaAssetId") @JoinColumn(name = "media_asset_id") private MediaAsset mediaAsset;
    @Column(name = "sort_order", nullable = false) private int sortOrder;

    private PortfolioProjectMedia(PortfolioProject project, MediaAsset mediaAsset, int sortOrder) {
        if (sortOrder < 0) throw new IllegalArgumentException("sortOrder must be nonnegative");
        this.project = Objects.requireNonNull(project); this.mediaAsset = Objects.requireNonNull(mediaAsset);
        this.id = new PortfolioProjectMediaId(project.getId(), mediaAsset.getId()); this.sortOrder = sortOrder;
    }
    public static PortfolioProjectMedia attach(PortfolioProject project, MediaAsset mediaAsset, int sortOrder) { return new PortfolioProjectMedia(project, mediaAsset, sortOrder); }
}
