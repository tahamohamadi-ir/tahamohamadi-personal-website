package ir.tahamohamadi.blog.post.api.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.tahamohamadi.audit.event.AuditEvent;
import ir.tahamohamadi.audit.event.AuditEventRepository;
import ir.tahamohamadi.blog.category.BlogCategory;
import ir.tahamohamadi.blog.category.BlogCategoryRepository;
import ir.tahamohamadi.blog.post.BlogPost;
import ir.tahamohamadi.blog.post.BlogPostRepository;
import ir.tahamohamadi.blog.post.BlogPostTranslation;
import ir.tahamohamadi.blog.post.BlogPostTranslationRepository;
import ir.tahamohamadi.blog.tag.BlogPostTag;
import ir.tahamohamadi.blog.tag.BlogPostTagRepository;
import ir.tahamohamadi.blog.tag.Tag;
import ir.tahamohamadi.blog.tag.TagRepository;
import ir.tahamohamadi.common.audit.AuthenticatedAuditActor;
import ir.tahamohamadi.common.domain.LanguageCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@Transactional
public class AdminBlogService {
    private final BlogPostRepository posts; private final BlogPostTranslationRepository translations; private final BlogCategoryRepository categories; private final TagRepository tags; private final BlogPostTagRepository postTags;
    private final AuditEventRepository audit; private final ObjectMapper mapper; private final AuthenticatedAuditActor actor;

    public AdminBlogService(BlogPostRepository posts, BlogPostTranslationRepository translations, BlogCategoryRepository categories, TagRepository tags, BlogPostTagRepository postTags, AuditEventRepository audit, ObjectMapper mapper, AuthenticatedAuditActor actor) {
        this.posts=posts; this.translations=translations; this.categories=categories; this.tags=tags; this.postTags=postTags; this.audit=audit; this.mapper=mapper; this.actor=actor;
    }

    @Transactional(readOnly=true) public Page<AdminBlogSummary> list(Pageable pageable) { return posts.findByDeletedAtIsNullOrderByUpdatedAtDescIdDesc(pageable).map(value->new AdminBlogSummary(value.getId(),value.getCategory().getId(),value.getStatus().name(),value.getVersion())); }
    @Transactional(readOnly=true) public AdminBlogResponse get(UUID id) { return response(post(id)); }

    public AdminBlogResponse create(AdminBlogCreateRequest request) {
        actor.required();
        BlogPost post=posts.save(BlogPost.create(UUID.randomUUID(),category(request.categoryId()),Instant.now()));
        save(post,request.fa(),request.en()); replaceTags(post,request.tagIds()); posts.flush(); record("ADMIN_BLOG_POST_CREATED",post.getId()); return response(post);
    }

    public AdminBlogResponse update(UUID id,AdminBlogUpdateRequest request) {
        BlogPost post=post(id); version(post,request.version()); post.updateCategory(category(request.categoryId()));
        save(post,request.fa(),request.en()); replaceTags(post,request.tagIds()); posts.flush(); record("ADMIN_BLOG_POST_UPDATED",id); return response(post);
    }

    public AdminBlogResponse publish(UUID id,long version) {
        BlogPost post=post(id); version(post,version); requirePublishable(post); post.publish(Instant.now());
        posts.flush(); record("ADMIN_BLOG_POST_PUBLISHED",id); return response(post);
    }

    public AdminBlogResponse archive(UUID id,long version) {
        BlogPost post=post(id); version(post,version); post.archive(); posts.flush(); record("ADMIN_BLOG_POST_ARCHIVED",id); return response(post);
    }

    public void delete(UUID id,long version) {
        BlogPost post=post(id); version(post,version); post.softDelete(actor.required(),Instant.now()); posts.flush(); record("ADMIN_BLOG_POST_DELETED",id);
    }

    private BlogPost post(UUID id) { return posts.findById(id).filter(value->value.getDeletedAt()==null).orElseThrow(()->new NoSuchElementException("Blog post not found")); }
    private BlogCategory category(UUID id) { return categories.findById(id).filter(value->value.getDeletedAt()==null&&value.isActive()).orElseThrow(()->new NoSuchElementException("Category not found")); }
    private void save(BlogPost post,BlogTranslationRequest fa,BlogTranslationRequest en) { save(post,LanguageCode.fa,fa); save(post,LanguageCode.en,en); }
    private void save(BlogPost post,LanguageCode language,BlogTranslationRequest request) { translations.findByBlogPostIdAndDeletedAtIsNull(post.getId()).stream().filter(value->value.getLanguageCode()==language).findFirst().ifPresentOrElse(value->value.update(request.title(),request.slug(),request.excerpt(),request.bodyMarkdown(),request.seoTitle(),request.seoDescription()),()->translations.save(BlogPostTranslation.create(UUID.randomUUID(),post,language,request.title(),request.slug(),request.bodyMarkdown(),Instant.now()))); }
    private void replaceTags(BlogPost post,List<UUID> tagIds) { if(tagIds==null) return; List<UUID> ids=tagIds.stream().distinct().toList(); if(ids.size()!=tagIds.size()) throw new IllegalArgumentException("Tag ids must be unique"); List<Tag> values=tags.findAllById(ids); if(values.size()!=ids.size()||values.stream().anyMatch(value->value.getDeletedAt()!=null||!value.isActive())) throw new NoSuchElementException("Tag not found"); postTags.deleteByIdBlogPostId(post.getId()); postTags.saveAll(values.stream().map(value->BlogPostTag.assign(post,value)).toList()); }
    private void requirePublishable(BlogPost post) { if(!post.getCategory().isActive()) throw new PublishValidationException(); List<BlogPostTranslation> values=translations.findByBlogPostIdAndDeletedAtIsNull(post.getId()); for(LanguageCode language:LanguageCode.values()) { BlogTranslationRequest translation=dto(values,language); if(blank(translation.seoTitle())||blank(translation.seoDescription())) throw new PublishValidationException(); } }
    private static boolean blank(String value) { return value==null||value.isBlank(); }
    private static void version(BlogPost post,long version) { if(post.getVersion()!=version) throw new ObjectOptimisticLockingFailureException(BlogPost.class,post.getId()); }
    private AdminBlogResponse response(BlogPost post) { List<BlogPostTranslation> values=translations.findByBlogPostIdAndDeletedAtIsNull(post.getId()); List<UUID> tagIds=postTags.findByPostIdWithTagOrderByTagKey(post.getId()).stream().map(value->value.getTag().getId()).toList(); return new AdminBlogResponse(post.getId(),post.getCategory().getId(),post.getStatus().name(),dto(values,LanguageCode.fa),dto(values,LanguageCode.en),tagIds,post.getVersion()); }
    private BlogTranslationRequest dto(List<BlogPostTranslation> values,LanguageCode language) { BlogPostTranslation value=values.stream().filter(item->item.getLanguageCode()==language).findFirst().orElseThrow(()->new NoSuchElementException("Blog translation not found")); return new BlogTranslationRequest(value.getTitle(),value.getSlug(),value.getExcerpt(),value.getBodyMarkdown(),value.getSeoTitle(),value.getSeoDescription()); }
    private void record(String action,UUID id) { audit.save(AuditEvent.record(UUID.randomUUID(),Instant.now(),actor.required(),action,"BLOG_POST",id,"SUCCESS",null,null,mapper.createObjectNode().put("changedFields","managed"))); }
}
