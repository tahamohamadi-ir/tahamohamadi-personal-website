package ir.tahamohamadi.portfolio.project;

import ir.tahamohamadi.common.domain.LanguageCode;
import ir.tahamohamadi.common.persistence.AuditedSoftDeletableEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "portfolio_project_translation")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PortfolioProjectTranslation extends AuditedSoftDeletableEntity {
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "portfolio_project_id", nullable = false) private PortfolioProject project;
    @Enumerated(EnumType.STRING) @Column(name = "language_code", nullable = false, length = 2) private LanguageCode languageCode;
    @Column(nullable = false) private String title;
    @Column(nullable = false) private String slug;
    @Column(columnDefinition = "text") private String summary;
    @Column(name = "body_markdown", columnDefinition = "text") private String bodyMarkdown;
    @Column(name = "seo_title") private String seoTitle;
    @Column(name = "seo_description") private String seoDescription;

    private PortfolioProjectTranslation(UUID id, PortfolioProject project, LanguageCode language, String title, String slug, String summary, String bodyMarkdown, String seoTitle, String seoDescription, Instant at) {
        initialize(id, at);
        this.project = project;
        this.languageCode = language;
        update(title, slug, summary, bodyMarkdown, seoTitle, seoDescription, at);
    }

    public static PortfolioProjectTranslation create(UUID id, PortfolioProject project, LanguageCode language, String title, String slug, String summary, String bodyMarkdown, String seoTitle, String seoDescription, Instant at) {
        return new PortfolioProjectTranslation(id, project, language, title, slug, summary, bodyMarkdown, seoTitle, seoDescription, at);
    }

    public void update(String title, String slug, String summary, String bodyMarkdown, String seoTitle, String seoDescription, Instant at) {
        this.title = requireNonBlank(title, "title");
        this.slug = requireNonBlank(slug, "slug");
        this.summary = summary;
        this.bodyMarkdown = bodyMarkdown;
        this.seoTitle = seoTitle;
        this.seoDescription = seoDescription;
        updatedAt = at;
    }
}
