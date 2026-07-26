package ir.tahamohamadi.blog.post;

import ir.tahamohamadi.common.domain.LanguageCode;
import ir.tahamohamadi.common.persistence.AuditedSoftDeletableEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "blog_post_translation")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BlogPostTranslation extends AuditedSoftDeletableEntity {
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "blog_post_id", nullable = false) private BlogPost blogPost;
    @Enumerated(EnumType.STRING) @Column(name = "language_code", nullable = false, length = 2) private LanguageCode languageCode;
    @Column(nullable = false, length = 255) private String title;
    @Column(nullable = false, length = 255) private String slug;
    @Column(columnDefinition = "text") private String excerpt;
    @Column(name = "body_markdown", nullable = false, columnDefinition = "text") private String bodyMarkdown;
    @Column(name = "article_document_json", columnDefinition = "text") private String articleDocumentJson;
    @Column(name = "seo_title", length = 255) private String seoTitle;
    @Column(name = "seo_description", length = 500) private String seoDescription;
    @Column(name = "search_vector", insertable = false, updatable = false, columnDefinition = "tsvector") private String searchVector;

    private BlogPostTranslation(UUID id, BlogPost post, LanguageCode lang, String title, String slug, String excerpt, String body, String articleDocumentJson, String seoTitle, String seoDescription, Instant at) {
        initialize(id, at);
        blogPost = Objects.requireNonNull(post);
        languageCode = Objects.requireNonNull(lang);
        apply(title, slug, excerpt, body, articleDocumentJson, seoTitle, seoDescription);
    }

    public static BlogPostTranslation create(UUID id, BlogPost post, LanguageCode lang, String title, String slug, String excerpt, String body, String articleDocumentJson, String seoTitle, String seoDescription, Instant at) { return new BlogPostTranslation(id, post, lang, title, slug, excerpt, body, articleDocumentJson, seoTitle, seoDescription, at); }

    public boolean update(String title, String slug, String excerpt, String body, String articleDocumentJson, String seoTitle, String seoDescription, Instant at) {
        boolean changed = !Objects.equals(this.title, title) || !Objects.equals(this.slug, slug) || !Objects.equals(this.excerpt, excerpt) || !Objects.equals(this.bodyMarkdown, body) || !Objects.equals(this.articleDocumentJson, articleDocumentJson) || !Objects.equals(this.seoTitle, seoTitle) || !Objects.equals(this.seoDescription, seoDescription);
        if (changed) {
            apply(title, slug, excerpt, body, articleDocumentJson, seoTitle, seoDescription);
            updatedAt = Objects.requireNonNull(at);
        }
        return changed;
    }

    private void apply(String title, String slug, String excerpt, String body, String articleDocumentJson, String seoTitle, String seoDescription) {
        this.title = requireNonBlank(title, "title");
        this.slug = requireNonBlank(slug, "slug");
        this.excerpt = excerpt;
        this.bodyMarkdown = requireNonBlank(body, "bodyMarkdown");
        this.articleDocumentJson = articleDocumentJson;
        this.seoTitle = seoTitle;
        this.seoDescription = seoDescription;
    }
}
