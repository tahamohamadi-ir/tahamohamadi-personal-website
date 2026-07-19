package ir.tahamohamadi.portfolio.project;

import ir.tahamohamadi.common.domain.ContentStatus;
import ir.tahamohamadi.common.persistence.AuditedSoftDeletableEntity;
import ir.tahamohamadi.media.asset.MediaAsset;
import jakarta.persistence.*;
import lombok.*;

import java.time.*;
import java.util.*;

@Entity
@Table(name = "portfolio_project")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PortfolioProject extends AuditedSoftDeletableEntity {
    @Column(name = "project_key", nullable = false) private String projectKey;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "cover_media_id") private MediaAsset coverMedia;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private ContentStatus status;
    @Column(name = "started_on") private LocalDate startedOn;
    @Column(name = "ended_on") private LocalDate endedOn;
    @Column(name = "project_url") private String projectUrl;
    @Column(name = "repository_url") private String repositoryUrl;
    @Column(name = "sort_order", nullable = false) private int sortOrder;

    private PortfolioProject(UUID id, String key, MediaAsset coverMedia, LocalDate started, LocalDate ended, String projectUrl, String repositoryUrl, int order, Instant at) {
        initialize(id, at);
        apply(key, coverMedia, started, ended, projectUrl, repositoryUrl, order, at);
        status = ContentStatus.DRAFT;
    }

    public static PortfolioProject create(UUID id, String key, MediaAsset coverMedia, LocalDate started, LocalDate ended, String projectUrl, String repositoryUrl, int order, Instant at) {
        return new PortfolioProject(id, key, coverMedia, started, ended, projectUrl, repositoryUrl, order, at);
    }

    public void update(String key, MediaAsset coverMedia, LocalDate started, LocalDate ended, String projectUrl, String repositoryUrl, int order, Instant at) {
        apply(key, coverMedia, started, ended, projectUrl, repositoryUrl, order, at);
    }

    public void publish(Instant at) {
        if (status != ContentStatus.DRAFT) throw new IllegalStateException("Only draft projects can be published");
        status = ContentStatus.PUBLISHED;
        updatedAt = at;
    }

    public void archive(Instant at) {
        if (status != ContentStatus.PUBLISHED) throw new IllegalStateException("Only published projects can be archived");
        status = ContentStatus.ARCHIVED;
        updatedAt = at;
    }

    private void apply(String key, MediaAsset cover, LocalDate started, LocalDate ended, String project, String repository, int order, Instant at) {
        projectKey = requireNonBlank(key, "projectKey");
        if (ended != null && started != null && ended.isBefore(started)) throw new IllegalArgumentException("endedOn must not precede startedOn");
        if (order < 0) throw new IllegalArgumentException("sortOrder must be nonnegative");
        coverMedia = cover;
        startedOn = started;
        endedOn = ended;
        projectUrl = project;
        repositoryUrl = repository;
        sortOrder = order;
        updatedAt = at;
    }
}
