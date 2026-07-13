package ir.tahamohamadi.content.featured.api.publicsite;

import ir.tahamohamadi.blog.post.*;
import ir.tahamohamadi.common.domain.*;
import ir.tahamohamadi.content.featured.*;
import ir.tahamohamadi.portfolio.project.*;
import ir.tahamohamadi.publication.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.*;

@Service
@Transactional(readOnly=true)
public class PublicFeaturedService {
    private final FeaturedItemRepository featured; private final BlogPostTranslationRepository posts; private final PortfolioProjectTranslationRepository projects; private final PublicationTranslationRepository publications;
    public PublicFeaturedService(FeaturedItemRepository featured, BlogPostTranslationRepository posts, PortfolioProjectTranslationRepository projects, PublicationTranslationRepository publications) { this.featured=featured; this.posts=posts; this.projects=projects; this.publications=publications; }
    public PublicFeaturedResponse list(LanguageCode locale,String slot,int size) { List<PublicFeaturedItemResponse> items=featured.findVisibleBySlot(slot,Instant.now(),PageRequest.of(0,size)).stream().map(value -> response(value,locale)).flatMap(Optional::stream).toList(); return new PublicFeaturedResponse(locale.name(),slot,items); }
    private Optional<PublicFeaturedItemResponse> response(FeaturedItem value,LanguageCode locale) { if(value.getBlogPost()!=null) { BlogPost post=value.getBlogPost(); if(post.getDeletedAt()!=null || post.getStatus()!=ContentStatus.PUBLISHED) return Optional.empty(); return posts.findByBlogPostIdAndDeletedAtIsNull(post.getId()).stream().filter(t -> t.getLanguageCode()==locale).findFirst().map(t -> new PublicFeaturedItemResponse("BLOG_POST",t.getSlug(),t.getTitle(),value.getSortOrder())); } if(value.getPortfolioProject()!=null) { PortfolioProject project=value.getPortfolioProject(); if(project.getDeletedAt()!=null || project.getStatus()!=ContentStatus.PUBLISHED) return Optional.empty(); return projects.findByProjectIdAndDeletedAtIsNull(project.getId()).stream().filter(t -> t.getLanguageCode()==locale).findFirst().map(t -> new PublicFeaturedItemResponse("PORTFOLIO_PROJECT",t.getSlug(),t.getTitle(),value.getSortOrder())); } Publication publication=value.getPublication(); if(publication==null || publication.getDeletedAt()!=null || publication.getContentStatus()!=ContentStatus.PUBLISHED) return Optional.empty(); return publications.findByPublicationIdAndDeletedAtIsNull(publication.getId()).stream().filter(t -> t.getLanguageCode()==locale).findFirst().map(t -> new PublicFeaturedItemResponse("PUBLICATION",t.getSlug(),t.getTitle(),value.getSortOrder())); }
}
