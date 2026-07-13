package ir.tahamohamadi.content.featured.api.publicsite;

import ir.tahamohamadi.blog.post.BlogPost;
import ir.tahamohamadi.blog.post.BlogPostTranslation;
import ir.tahamohamadi.blog.post.BlogPostTranslationRepository;
import ir.tahamohamadi.common.domain.ContentStatus;
import ir.tahamohamadi.common.domain.LanguageCode;
import ir.tahamohamadi.content.featured.FeaturedItem;
import ir.tahamohamadi.content.featured.FeaturedItemRepository;
import ir.tahamohamadi.portfolio.project.PortfolioProject;
import ir.tahamohamadi.portfolio.project.PortfolioProjectTranslation;
import ir.tahamohamadi.portfolio.project.PortfolioProjectTranslationRepository;
import ir.tahamohamadi.publication.Publication;
import ir.tahamohamadi.publication.PublicationTranslation;
import ir.tahamohamadi.publication.PublicationTranslationRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class PublicFeaturedService {
    private final FeaturedItemRepository featured;
    private final BlogPostTranslationRepository posts;
    private final PortfolioProjectTranslationRepository projects;
    private final PublicationTranslationRepository publications;

    public PublicFeaturedService(FeaturedItemRepository featured, BlogPostTranslationRepository posts, PortfolioProjectTranslationRepository projects, PublicationTranslationRepository publications) {
        this.featured = featured;
        this.posts = posts;
        this.projects = projects;
        this.publications = publications;
    }

    public PublicFeaturedResponse list(LanguageCode locale, String slot, int size) {
        Instant now = Instant.now();
        List<FeaturedItem> values = featured.findVisibleBySlot(slot, now, PageRequest.of(0, size));
        Map<UUID, BlogPostTranslation> postTranslations = byPostId(ids(values, FeaturedItem::getBlogPost), locale);
        Map<UUID, PortfolioProjectTranslation> projectTranslations = byProjectId(ids(values, FeaturedItem::getPortfolioProject), locale);
        Map<UUID, PublicationTranslation> publicationTranslations = byPublicationId(ids(values, FeaturedItem::getPublication), locale);
        List<PublicFeaturedItemResponse> items = values.stream()
                .map(value -> response(value, now, postTranslations, projectTranslations, publicationTranslations))
                .flatMap(java.util.Optional::stream)
                .toList();
        String path = "/" + locale + "/featured";
        return new PublicFeaturedResponse(locale.name(), List.of(locale.name()), path, List.of(new PublicFeaturedLink(locale.name(), path)), new PublicFeaturedSeo(null, null), null, null, slot, items);
    }

    private Map<UUID, BlogPostTranslation> byPostId(List<UUID> ids, LanguageCode locale) {
        if (ids.isEmpty()) return Map.of();
        return posts.findByBlogPostIdInAndLanguageCodeAndDeletedAtIsNull(ids, locale).stream()
                .collect(Collectors.toMap(translation -> translation.getBlogPost().getId(), Function.identity()));
    }

    private Map<UUID, PortfolioProjectTranslation> byProjectId(List<UUID> ids, LanguageCode locale) {
        if (ids.isEmpty()) return Map.of();
        return projects.findByProjectIdInAndLanguageCodeAndDeletedAtIsNull(ids, locale).stream()
                .collect(Collectors.toMap(translation -> translation.getProject().getId(), Function.identity()));
    }

    private Map<UUID, PublicationTranslation> byPublicationId(List<UUID> ids, LanguageCode locale) {
        if (ids.isEmpty()) return Map.of();
        return publications.findByPublicationIdInAndLanguageCodeAndDeletedAtIsNull(ids, locale).stream()
                .collect(Collectors.toMap(translation -> translation.getPublication().getId(), Function.identity()));
    }

    private static <T> List<UUID> ids(Collection<FeaturedItem> values, Function<FeaturedItem, T> target) {
        return values.stream().map(target).filter(Objects::nonNull).map(value -> {
            if (value instanceof BlogPost post) return post.getId();
            if (value instanceof PortfolioProject project) return project.getId();
            return ((Publication) value).getId();
        }).toList();
    }

    private java.util.Optional<PublicFeaturedItemResponse> response(FeaturedItem value, Instant now, Map<UUID, BlogPostTranslation> postTranslations, Map<UUID, PortfolioProjectTranslation> projectTranslations, Map<UUID, PublicationTranslation> publicationTranslations) {
        if (value.getBlogPost() != null) {
            BlogPost post = value.getBlogPost();
            if (post.getDeletedAt() != null || post.getStatus() != ContentStatus.PUBLISHED || post.getPublishedAt() == null || post.getPublishedAt().isAfter(now)) return java.util.Optional.empty();
            BlogPostTranslation translation = postTranslations.get(post.getId());
            return translation == null ? java.util.Optional.empty() : java.util.Optional.of(new PublicFeaturedItemResponse("BLOG_POST", translation.getSlug(), translation.getTitle(), value.getSortOrder()));
        }
        if (value.getPortfolioProject() != null) {
            PortfolioProject project = value.getPortfolioProject();
            if (project.getDeletedAt() != null || project.getStatus() != ContentStatus.PUBLISHED) return java.util.Optional.empty();
            PortfolioProjectTranslation translation = projectTranslations.get(project.getId());
            return translation == null ? java.util.Optional.empty() : java.util.Optional.of(new PublicFeaturedItemResponse("PORTFOLIO_PROJECT", translation.getSlug(), translation.getTitle(), value.getSortOrder()));
        }
        Publication publication = value.getPublication();
        if (publication == null || publication.getDeletedAt() != null || publication.getContentStatus() != ContentStatus.PUBLISHED) return java.util.Optional.empty();
        PublicationTranslation translation = publicationTranslations.get(publication.getId());
        return translation == null ? java.util.Optional.empty() : java.util.Optional.of(new PublicFeaturedItemResponse("PUBLICATION", translation.getSlug(), translation.getTitle(), value.getSortOrder()));
    }
}
