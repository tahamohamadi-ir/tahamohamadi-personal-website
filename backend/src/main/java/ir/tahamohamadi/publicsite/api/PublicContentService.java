package ir.tahamohamadi.publicsite.api;

import ir.tahamohamadi.blog.post.*;
import ir.tahamohamadi.common.domain.LanguageCode;
import ir.tahamohamadi.content.page.*;
import ir.tahamohamadi.content.social.*;
import ir.tahamohamadi.portfolio.project.*;
import ir.tahamohamadi.publication.*;
import ir.tahamohamadi.resume.*;
import org.springframework.stereotype.Service;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service @Transactional(readOnly = true) @ConditionalOnExpression("!'${spring.autoconfigure.exclude:}'.contains('DataSourceAutoConfiguration')")
public class PublicContentService {
    private final ContentPageTranslationRepository pages; private final BlogPostTranslationRepository posts; private final PortfolioProjectTranslationRepository projects; private final PublicationTranslationRepository publications; private final ResumeEntryTranslationRepository resume; private final SocialLinkRepository social;
    public PublicContentService(ContentPageTranslationRepository pages,BlogPostTranslationRepository posts,PortfolioProjectTranslationRepository projects,PublicationTranslationRepository publications,ResumeEntryTranslationRepository resume,SocialLinkRepository social){this.pages=pages;this.posts=posts;this.projects=projects;this.publications=publications;this.resume=resume;this.social=social;}
    public Map<String,Object> page(LanguageCode lang,String slug){return pageDto(pages.findPublishedByLanguageAndSlug(lang,slug).orElseThrow(()->new NoSuchElementException("Page not found")));}
    public List<Map<String,Object>> posts(LanguageCode lang,String q,int page,int size){List<BlogPostTranslation> values=q==null||q.isBlank()?posts.findPublishedByLanguage(lang,org.springframework.data.domain.PageRequest.of(page,size)):posts.searchPublishedByLanguage(lang.name(),q);return values.stream().skip((long)page*size).limit(size).map(this::postDto).toList();}
    public Map<String,Object> post(LanguageCode lang,String slug){return postDto(posts.findPublishedByLanguageAndSlug(lang,slug).orElseThrow(()->new NoSuchElementException("Post not found")));}
    public List<Map<String,Object>> projects(LanguageCode lang){return projects.findPublishedByLanguage(lang).stream().map(this::projectDto).toList();}
    public Map<String,Object> project(LanguageCode lang,String slug){return projectDto(projects.findPublishedByLanguageAndSlug(lang,slug).orElseThrow(()->new NoSuchElementException("Project not found")));}
    public List<Map<String,Object>> publications(LanguageCode lang){return publications.findPublishedByLanguage(lang).stream().map(this::publicationDto).toList();}
    public Map<String,Object> publication(LanguageCode lang,String slug){return publicationDto(publications.findPublishedByLanguageAndSlug(lang,slug).orElseThrow(()->new NoSuchElementException("Publication not found")));}
    public List<Map<String,Object>> resume(LanguageCode lang){return resume.findPublishedByLanguage(lang).stream().map(t->Map.<String,Object>of("title",t.getTitle(),"organization",t.getOrganization(),"summary",value(t.getSummary()),"type",t.getEntry().getEntryType().name(),"startedOn",t.getEntry().getStartedOn(),"endedOn",value(t.getEntry().getEndedOn()),"current",t.getEntry().isCurrent())).toList();}
    public List<Map<String,Object>> social(){return social.findByActiveTrueAndDeletedAtIsNullOrderBySortOrderAscIdAsc().stream().map(s->Map.<String,Object>of("platform",s.getPlatformCode(),"url",s.getUrl())).toList();}
    private Map<String,Object> pageDto(ContentPageTranslation t){return Map.of("slug",t.getSlug(),"title",t.getTitle(),"summary",value(t.getSummary()),"bodyMarkdown",value(t.getBodyMarkdown()),"seo",seo(t.getSeoTitle(),t.getSeoDescription(),t.getCanonicalPath()));}
    private Map<String,Object> postDto(BlogPostTranslation t){return Map.of("slug",t.getSlug(),"title",t.getTitle(),"excerpt",value(t.getExcerpt()),"bodyMarkdown",value(t.getBodyMarkdown()),"publishedAt",t.getBlogPost().getPublishedAt(),"seo",seo(t.getSeoTitle(),t.getSeoDescription(),null));}
    private Map<String,Object> projectDto(PortfolioProjectTranslation t){return Map.of("slug",t.getSlug(),"title",t.getTitle(),"summary",value(t.getSummary()),"bodyMarkdown",value(t.getBodyMarkdown()),"seo",seo(t.getSeoTitle(),t.getSeoDescription(),null));}
    private Map<String,Object> publicationDto(PublicationTranslation t){return Map.of("slug",t.getSlug(),"title",t.getTitle(),"abstract",value(t.getAbstractText()),"authors",value(t.getAuthorsDisplay()),"venue",value(t.getVenueDisplay()),"year",t.getPublication().getYear(),"stage",t.getPublication().getPublicationStage().name(),"seo",seo(t.getSeoTitle(),t.getSeoDescription(),null));}
    private static Map<String,Object> seo(String title,String description,String canonical){Map<String,Object> result=new LinkedHashMap<>();result.put("title",title);result.put("description",description);result.put("canonicalPath",canonical);return result;}
    private static Object value(Object value){return value==null?"":value;}
}
