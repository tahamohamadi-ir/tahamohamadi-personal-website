package ir.tahamohamadi.content.page;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
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
@Table(name = "content_page_revision")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ContentPageRevision {
    @Id private UUID id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "content_page_id", nullable = false) private ContentPage contentPage;
    @Column(name = "revision_number", nullable = false) private int revisionNumber;
    @Column(name = "snapshot_json", nullable = false, columnDefinition = "text") private String snapshotJson;
    @Column(nullable = false, length = 40) private String reason;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "created_by") private UUID createdBy;

    private ContentPageRevision(UUID id, ContentPage page, int revisionNumber, String snapshotJson, String reason, Instant createdAt, UUID createdBy) {
        this.id = Objects.requireNonNull(id); this.contentPage = Objects.requireNonNull(page); this.revisionNumber = revisionNumber;
        this.snapshotJson = Objects.requireNonNull(snapshotJson); this.reason = Objects.requireNonNull(reason);
        this.createdAt = Objects.requireNonNull(createdAt); this.createdBy = createdBy;
    }

    public static ContentPageRevision create(UUID id, ContentPage page, int revisionNumber, String snapshotJson, String reason, Instant createdAt, UUID createdBy) {
        return new ContentPageRevision(id, page, revisionNumber, snapshotJson, reason, createdAt, createdBy);
    }
}
