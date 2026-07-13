package ir.tahamohamadi.content.page;

import ir.tahamohamadi.common.domain.ContentStatus;
import ir.tahamohamadi.common.persistence.AuditedSoftDeletableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity @Table(name = "content_page") @Getter @NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ContentPage extends AuditedSoftDeletableEntity {
    @Column(name = "page_key", nullable = false, length = 100) private String pageKey;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private ContentStatus status;
    @Column(name = "published_at") private Instant publishedAt;
    private ContentPage(UUID id, String pageKey, Instant createdAt) { initialize(id, createdAt); this.pageKey = requireNonBlank(pageKey, "pageKey"); this.status = ContentStatus.DRAFT; }
    public static ContentPage create(UUID id, String pageKey, Instant createdAt) { return new ContentPage(id, pageKey, createdAt); }
    public void rename(String pageKey) { this.pageKey = requireNonBlank(pageKey, "pageKey"); }
    public void publish(Instant publishedAt) { this.status = ContentStatus.PUBLISHED; this.publishedAt = publishedAt; }
    public void archive() { this.status = ContentStatus.ARCHIVED; }
}
