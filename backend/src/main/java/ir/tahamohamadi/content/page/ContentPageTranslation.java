package ir.tahamohamadi.content.page;

import ir.tahamohamadi.common.domain.LanguageCode;
import ir.tahamohamadi.common.persistence.AuditedSoftDeletableEntity;
import ir.tahamohamadi.media.asset.MediaAsset;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity @Table(name = "content_page_translation") @Getter @NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ContentPageTranslation extends AuditedSoftDeletableEntity {
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "content_page_id", nullable = false) private ContentPage contentPage;
    @Enumerated(EnumType.STRING) @Column(name = "language_code", nullable = false, length = 2) private LanguageCode languageCode;
    @Column(nullable = false, length = 255) private String title;
    @Column(nullable = false, length = 255) private String slug;
    @Column(columnDefinition = "text") private String summary;
    @Column(name = "body_markdown", columnDefinition = "text") private String bodyMarkdown;
    @Column(name = "seo_title", length = 255) private String seoTitle;
    @Column(name = "seo_description", length = 500) private String seoDescription;
    @Column(name = "canonical_path", length = 500) private String canonicalPath;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "og_media_id") private MediaAsset ogMedia;
    private ContentPageTranslation(UUID id, ContentPage page, LanguageCode language, String title, String slug, Instant createdAt) { initialize(id, createdAt); this.contentPage=Objects.requireNonNull(page); this.languageCode=Objects.requireNonNull(language); this.title=requireNonBlank(title,"title"); this.slug=requireNonBlank(slug,"slug"); }
    public static ContentPageTranslation create(UUID id, ContentPage page, LanguageCode language, String title, String slug, Instant createdAt) { return new ContentPageTranslation(id,page,language,title,slug,createdAt); }
    public void update(String title, String slug, String summary, String bodyMarkdown, String seoTitle, String seoDescription, String canonicalPath) {
        this.title=requireNonBlank(title,"title"); this.slug=requireNonBlank(slug,"slug"); this.summary=summary; this.bodyMarkdown=bodyMarkdown;
        this.seoTitle=seoTitle; this.seoDescription=seoDescription; this.canonicalPath=canonicalPath;
    }
}
