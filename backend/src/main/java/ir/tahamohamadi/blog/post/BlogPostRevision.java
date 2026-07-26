package ir.tahamohamadi.blog.post;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "blog_post_revision")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BlogPostRevision {
    @Id private UUID id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "blog_post_id", nullable = false) private BlogPost blogPost;
    @Column(name = "revision_number", nullable = false) private int revisionNumber;
    @Column(name = "snapshot_json", nullable = false, columnDefinition = "text") private String snapshotJson;
    @Column(nullable = false, length = 40) private String reason;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "created_by") private UUID createdBy;

    private BlogPostRevision(UUID id, BlogPost post, int number, String snapshot, String reason, Instant at, UUID actor) {
        this.id = Objects.requireNonNull(id); this.blogPost = Objects.requireNonNull(post); this.revisionNumber = number;
        this.snapshotJson = Objects.requireNonNull(snapshot); this.reason = Objects.requireNonNull(reason);
        this.createdAt = Objects.requireNonNull(at); this.createdBy = actor;
    }
    public static BlogPostRevision create(UUID id, BlogPost post, int number, String snapshot, String reason, Instant at, UUID actor) {
        return new BlogPostRevision(id, post, number, snapshot, reason, at, actor);
    }
}
