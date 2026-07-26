package ir.tahamohamadi.blog.post.api.admin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.tahamohamadi.audit.event.AuditEvent;
import ir.tahamohamadi.audit.event.AuditEventRepository;
import ir.tahamohamadi.blog.category.BlogCategory;
import ir.tahamohamadi.blog.category.BlogCategoryRepository;
import ir.tahamohamadi.blog.post.BlogPost;
import ir.tahamohamadi.blog.post.BlogPostMedia;
import ir.tahamohamadi.blog.post.BlogPostMediaRepository;
import ir.tahamohamadi.blog.post.BlogPostRepository;
import ir.tahamohamadi.blog.post.BlogPostRevision;
import ir.tahamohamadi.blog.post.BlogPostRevisionRepository;
import ir.tahamohamadi.blog.post.BlogPostTranslation;
import ir.tahamohamadi.blog.post.BlogPostTranslationRepository;
import ir.tahamohamadi.blog.tag.BlogPostTag;
import ir.tahamohamadi.blog.tag.BlogPostTagRepository;
import ir.tahamohamadi.blog.tag.Tag;
import ir.tahamohamadi.blog.tag.TagRepository;
import ir.tahamohamadi.common.audit.AuthenticatedAuditActor;
import ir.tahamohamadi.common.domain.LanguageCode;
import ir.tahamohamadi.common.domain.TranslationStatus;
import ir.tahamohamadi.media.asset.MediaAsset;
import ir.tahamohamadi.media.asset.MediaAssetRepository;
import ir.tahamohamadi.media.asset.MediaAssetStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@Transactional
public class AdminBlogService {
    private final BlogPostRepository posts; private final BlogPostTranslationRepository translations; private final BlogCategoryRepository categories; private final TagRepository tags; private final BlogPostTagRepository postTags; private final MediaAssetRepository media; private final BlogPostMediaRepository postMedia; private final BlogPostRevisionRepository revisions;
    private final AuditEventRepository audit; private final ObjectMapper mapper; private final AuthenticatedAuditActor actor;

    public AdminBlogService(BlogPostRepository posts, BlogPostTranslationRepository translations, BlogCategoryRepository categories, TagRepository tags, BlogPostTagRepository postTags, MediaAssetRepository media, BlogPostMediaRepository postMedia, BlogPostRevisionRepository revisions, AuditEventRepository audit, ObjectMapper mapper, AuthenticatedAuditActor actor) {
        this.posts=posts; this.translations=translations; this.categories=categories; this.tags=tags; this.postTags=postTags; this.media=media; this.postMedia=postMedia; this.revisions=revisions; this.audit=audit; this.mapper=mapper; this.actor=actor;
    }

    @Transactional(readOnly=true) public Page<AdminBlogSummary> list(Pageable pageable) { return posts.findByDeletedAtIsNullOrderByUpdatedAtDescIdDesc(pageable).map(value->new AdminBlogSummary(value.getId(),value.getCategory().getId(),value.getStatus().name(),value.getVersion())); }
    @Transactional(readOnly=true) public AdminBlogResponse get(UUID id) { return response(post(id)); }

    public AdminBlogResponse create(AdminBlogCreateRequest request, LanguageCode sourceLanguage) {
        actor.required();
        Instant now=Instant.now(); BlogPost post=posts.save(BlogPost.create(UUID.randomUUID(),category(request.categoryId()),now)); post.updateSourceLanguage(sourceLanguage,now);
        replaceTags(post,request.tagIds()); replaceMedia(post,request.media()); save(post,request.fa(),request.en()); posts.flush(); snapshot(post,"CREATED"); record("ADMIN_BLOG_POST_CREATED",post.getId()); return response(post);
    }

    public AdminBlogResponse update(UUID id,AdminBlogUpdateRequest request, LanguageCode sourceLanguage) {
        BlogPost post=post(id); version(post,request.version()); snapshot(post,"BEFORE_UPDATE"); post.updateCategory(category(request.categoryId())); if(sourceLanguage!=null) post.updateSourceLanguage(sourceLanguage,Instant.now());
        replaceTags(post,request.tagIds()); replaceMedia(post,request.media()); save(post,request.fa(),request.en()); posts.flush(); record("ADMIN_BLOG_POST_UPDATED",id); return response(post);
    }

    public AdminBlogResponse submitForReview(UUID id, long version) {
        BlogPost post=post(id); version(post,version); snapshot(post,"BEFORE_REVIEW"); post.submitForReview(Instant.now());
        posts.flush(); record("ADMIN_BLOG_POST_SUBMITTED_FOR_REVIEW",id); return response(post);
    }

    public AdminBlogResponse returnToDraft(UUID id, long version) {
        BlogPost post=post(id); version(post,version); snapshot(post,"BEFORE_RETURN_TO_DRAFT"); post.returnToDraft(Instant.now());
        posts.flush(); record("ADMIN_BLOG_POST_RETURNED_TO_DRAFT",id); return response(post);
    }

    public AdminBlogResponse publish(UUID id,long version) {
        BlogPost post=post(id); version(post,version); snapshot(post,"BEFORE_PUBLISH"); requirePublishable(post); post.publish(Instant.now());
        posts.flush(); record("ADMIN_BLOG_POST_PUBLISHED",id); return response(post);
    }

    public AdminBlogResponse schedule(UUID id, long version, Instant scheduledFor) {
        BlogPost post=post(id); version(post,version); requirePublishable(post); Instant now=Instant.now(); snapshot(post,"BEFORE_SCHEDULE"); post.schedule(now,scheduledFor);
        posts.flush(); record("ADMIN_BLOG_POST_SCHEDULED",id); return response(post);
    }

    public AdminBlogResponse cancelSchedule(UUID id, long version) {
        BlogPost post=post(id); version(post,version); snapshot(post,"BEFORE_CANCEL_SCHEDULE"); post.cancelSchedule(Instant.now());
        posts.flush(); record("ADMIN_BLOG_POST_SCHEDULE_CANCELLED",id); return response(post);
    }

    public AdminBlogResponse archive(UUID id,long version) {
        BlogPost post=post(id); version(post,version); snapshot(post,"BEFORE_ARCHIVE"); post.archive(); posts.flush(); record("ADMIN_BLOG_POST_ARCHIVED",id); return response(post);
    }

    public void delete(UUID id,long version) {
        BlogPost post=post(id); version(post,version); post.softDelete(actor.required(),Instant.now()); posts.flush(); record("ADMIN_BLOG_POST_DELETED",id);
    }

    private BlogPost post(UUID id) { return posts.findById(id).filter(value->value.getDeletedAt()==null).orElseThrow(()->new NoSuchElementException("Blog post not found")); }
    private BlogCategory category(UUID id) { return categories.findById(id).filter(value->value.getDeletedAt()==null&&value.isActive()).orElseThrow(()->new NoSuchElementException("Category not found")); }
    private void save(BlogPost post,BlogTranslationRequest fa,BlogTranslationRequest en) { save(post,LanguageCode.fa,fa); save(post,LanguageCode.en,en); }
    private void save(BlogPost post,LanguageCode language,BlogTranslationRequest request) { Instant now=Instant.now(); String document = document(post, request.articleDocument()); translations.findByBlogPostIdAndDeletedAtIsNull(post.getId()).stream().filter(value->value.getLanguageCode()==language).findFirst().ifPresentOrElse(value->value.update(request.title(),request.slug(),request.excerpt(),request.bodyMarkdown(),document,request.seoTitle(),request.seoDescription(),now),()->translations.save(BlogPostTranslation.create(UUID.randomUUID(),post,language,request.title(),request.slug(),request.excerpt(),request.bodyMarkdown(),document,request.seoTitle(),request.seoDescription(),now))); }
    private void replaceTags(BlogPost post,List<UUID> tagIds) { if(tagIds==null) return; List<UUID> ids=tagIds.stream().distinct().toList(); if(ids.size()!=tagIds.size()) throw new IllegalArgumentException("Tag ids must be unique"); List<Tag> values=tags.findAllById(ids); if(values.size()!=ids.size()||values.stream().anyMatch(value->value.getDeletedAt()!=null||!value.isActive())) throw new NoSuchElementException("Tag not found"); postTags.deleteByIdBlogPostId(post.getId()); postTags.saveAll(values.stream().map(value->BlogPostTag.assign(post,value)).toList()); }
    private void replaceMedia(BlogPost post, List<AdminBlogMediaReferenceRequest> references) {
        if (references == null) return;
        if (references.stream().map(reference -> reference.mediaAssetId() + ":" + reference.usage()).distinct().count() != references.size()) {
            throw new IllegalArgumentException("Media references must be unique per usage");
        }
        if (references.stream().map(reference -> reference.usage() + ":" + reference.sortOrder()).distinct().count() != references.size()) {
            throw new IllegalArgumentException("Media sort order must be unique per usage");
        }

        List<UUID> ids = references.stream()
                .map(AdminBlogMediaReferenceRequest::mediaAssetId)
                .distinct()
                .toList();
        Map<UUID, MediaAsset> activeMedia = media.findAllById(ids).stream()
                .filter(asset -> asset.getDeletedAt() == null && asset.getStatus() == MediaAssetStatus.ACTIVE)
                .collect(Collectors.toMap(MediaAsset::getId, asset -> asset));

        if (activeMedia.size() != ids.size()) {
            throw new NoSuchElementException("Media asset not found");
        }

        List<MediaAsset> resolved = references.stream()
                .map(reference -> activeMedia.get(reference.mediaAssetId()))
                .toList();

        postMedia.deleteAllByPostId(post.getId());
        postMedia.flush();
        postMedia.saveAll(java.util.stream.IntStream.range(0, references.size())
                .mapToObj(index -> BlogPostMedia.attach(
                        post,
                        resolved.get(index),
                        references.get(index).usage(),
                        references.get(index).sortOrder()
                ))
                .toList());
    }
    private void requirePublishable(BlogPost post) { if(post.getCategory().getDeletedAt()!=null||!post.getCategory().isActive()) throw new PublishValidationException(); List<BlogPostTranslation> values=translations.findByBlogPostIdAndDeletedAtIsNull(post.getId()); for(LanguageCode language:LanguageCode.values()) { BlogTranslationRequest translation=dto(values,language); if(blank(translation.seoTitle())||blank(translation.seoDescription())) throw new PublishValidationException(); } }
    private static boolean blank(String value) { return value==null||value.isBlank(); }
    private static void version(BlogPost post,long version) { if(post.getVersion()!=version) throw new ObjectOptimisticLockingFailureException(BlogPost.class,post.getId()); }
    private AdminBlogResponse response(BlogPost post) { List<BlogPostTranslation> values=translations.findByBlogPostIdAndDeletedAtIsNull(post.getId()); List<UUID> tagIds=postTags.findByPostIdWithTagOrderByTagKey(post.getId()).stream().map(value->value.getTag().getId()).toList(); List<AdminBlogMediaResponse> media=postMedia.findByPostIdWithAssetOrderByUsageAndSortOrder(post.getId()).stream().map(value->new AdminBlogMediaResponse(value.getMediaAsset().getId(),value.getId().getUsage().name(),value.getSortOrder())).toList(); return new AdminBlogResponse(post.getId(),post.getCategory().getId(),post.getStatus().name(),post.getScheduledFor(),post.getSourceLanguage(),translationStatus(values,LanguageCode.fa,post.getSourceLanguage()),translationStatus(values,LanguageCode.en,post.getSourceLanguage()),dto(values,LanguageCode.fa),dto(values,LanguageCode.en),tagIds,media,post.getVersion()); }
    private AdminBlogTranslationStatus translationStatus(List<BlogPostTranslation> values,LanguageCode language,LanguageCode sourceLanguage) { BlogPostTranslation value=values.stream().filter(item->item.getLanguageCode()==language).findFirst().orElse(null); BlogPostTranslation source=values.stream().filter(item->item.getLanguageCode()==sourceLanguage).findFirst().orElse(null); if(value==null) return new AdminBlogTranslationStatus(TranslationStatus.MISSING.name(),source==null?null:source.getUpdatedAt()); if(blank(value.getTitle())||blank(value.getSlug())||blank(value.getBodyMarkdown())||blank(value.getSeoTitle())||blank(value.getSeoDescription())) return new AdminBlogTranslationStatus(TranslationStatus.INCOMPLETE.name(),source==null?null:source.getUpdatedAt()); if(language!=sourceLanguage&&source!=null&&value.getUpdatedAt().isBefore(source.getUpdatedAt())) return new AdminBlogTranslationStatus(TranslationStatus.OUTDATED.name(),source.getUpdatedAt()); return new AdminBlogTranslationStatus(TranslationStatus.COMPLETE.name(),source==null?null:source.getUpdatedAt()); }
    private BlogTranslationRequest dto(List<BlogPostTranslation> values,LanguageCode language) { BlogPostTranslation value=values.stream().filter(item->item.getLanguageCode()==language).findFirst().orElseThrow(()->new NoSuchElementException("Blog translation not found")); return new BlogTranslationRequest(value.getTitle(),value.getSlug(),value.getExcerpt(),value.getBodyMarkdown(),readDocument(value.getArticleDocumentJson()),value.getSeoTitle(),value.getSeoDescription()); }
    private String document(BlogPost post, JsonNode value) { if (value == null || value.isNull()) return null; if (!value.isObject() || value.path("version").asInt(-1) != 1 || !value.path("blocks").isArray() || value.path("blocks").size() > 200) throw new IllegalArgumentException("Unsupported article document"); Set<UUID> inlineMedia = postMedia.findByBlogPostIdAndIdUsageOrderBySortOrderAsc(post.getId(), ir.tahamohamadi.blog.post.BlogPostMediaUsage.INLINE).stream().map(reference -> reference.getId().getMediaAssetId()).collect(Collectors.toSet()); for (JsonNode block : value.path("blocks")) { String type = block.path("type").asText(); if (!block.isObject() || !Set.of("paragraph","heading","quote","code","image","divider","markdown").contains(type)) throw new IllegalArgumentException("Unsupported article document"); if ("image".equals(type)) { if (block.has("src") || !block.path("mediaId").isTextual()) throw new IllegalArgumentException("Article image must reference managed inline media"); try { if (!inlineMedia.contains(UUID.fromString(block.path("mediaId").asText()))) throw new IllegalArgumentException("Article image must reference managed inline media"); } catch (IllegalArgumentException exception) { throw new IllegalArgumentException("Article image must reference managed inline media", exception); } } } try { String raw=mapper.writeValueAsString(value); if(raw.length()>100000)throw new IllegalArgumentException("Article document is too large"); return raw; } catch (com.fasterxml.jackson.core.JsonProcessingException exception) { throw new IllegalArgumentException("Unsupported article document",exception); } }
    private JsonNode readDocument(String raw) { if(raw==null||raw.isBlank())return null; try{return mapper.readTree(raw);}catch(com.fasterxml.jackson.core.JsonProcessingException exception){throw new IllegalStateException("Stored article document is invalid",exception);} }
    @Transactional(readOnly = true) public List<AdminBlogRevisionSummary> revisions(UUID id) { post(id); return revisions.findByBlogPostIdOrderByRevisionNumberDesc(id).stream().map(value -> new AdminBlogRevisionSummary(value.getId(), value.getRevisionNumber(), value.getReason(), value.getCreatedAt(), value.getCreatedBy())).toList(); }
    @Transactional(readOnly = true) public AdminBlogRevisionResponse revisionDetail(UUID id, UUID revisionId) { return revisionResponse(revision(id, revisionId)); }
    public AdminBlogResponse restoreAsDraft(UUID id, UUID revisionId, long version) {
        BlogPost source = post(id); version(source, version); BlogPostRevision revision = revision(id, revisionId); AdminBlogResponse snapshot = snapshot(revision);
        BlogPost restored = posts.save(BlogPost.create(UUID.randomUUID(), category(snapshot.categoryId()), Instant.now()));
        replaceTags(restored, snapshot.tagIds());
        replaceMedia(restored, snapshot.media().stream().map(value -> new AdminBlogMediaReferenceRequest(value.mediaAssetId(), ir.tahamohamadi.blog.post.BlogPostMediaUsage.valueOf(value.usage()), value.sortOrder())).toList());
        save(restored, restoreTranslation(snapshot.fa(), revision.getRevisionNumber()), restoreTranslation(snapshot.en(), revision.getRevisionNumber()));
        posts.flush(); snapshot(restored, "RESTORED_DRAFT"); record("ADMIN_BLOG_POST_REVISION_RESTORED_AS_DRAFT", id); return response(restored);
    }
    private BlogPostRevision revision(UUID postId, UUID revisionId) { return revisions.findByIdAndBlogPostId(revisionId, postId).orElseThrow(() -> new NoSuchElementException("Blog revision not found")); }
    private AdminBlogRevisionResponse revisionResponse(BlogPostRevision value) { return new AdminBlogRevisionResponse(value.getId(), value.getRevisionNumber(), value.getReason(), value.getCreatedAt(), value.getCreatedBy(), snapshot(value)); }
    private AdminBlogResponse snapshot(BlogPostRevision revision) { try { return mapper.readValue(revision.getSnapshotJson(), AdminBlogResponse.class); } catch (com.fasterxml.jackson.core.JsonProcessingException exception) { throw new IllegalStateException("Stored blog revision is invalid", exception); } }
    private void snapshot(BlogPost post, String reason) { try { int next = revisions.findFirstByBlogPostIdOrderByRevisionNumberDesc(post.getId()).map(value -> value.getRevisionNumber() + 1).orElse(1); revisions.save(BlogPostRevision.create(UUID.randomUUID(), post, next, mapper.writeValueAsString(response(post)), reason, Instant.now(), actor.required().getId())); } catch (com.fasterxml.jackson.core.JsonProcessingException exception) { throw new IllegalStateException("Blog revision could not be recorded", exception); } }
    private BlogTranslationRequest restoreTranslation(BlogTranslationRequest source, int revisionNumber) { String suffix = "-revision-" + revisionNumber; return new BlogTranslationRequest(source.title(), uniqueSlug(source.slug(), suffix), source.excerpt(), source.bodyMarkdown(), source.articleDocument(), source.seoTitle(), source.seoDescription()); }
    private String uniqueSlug(String source, String suffix) { String base = source.length() + suffix.length() <= 255 ? source : source.substring(0, 255 - suffix.length()); String candidate = base + suffix; int attempt = 2; while (translations.existsActiveByLanguageAndSlug(LanguageCode.fa, candidate) || translations.existsActiveByLanguageAndSlug(LanguageCode.en, candidate)) { String numbered = suffix + "-" + attempt++; String shortened = source.length() + numbered.length() <= 255 ? source : source.substring(0, 255 - numbered.length()); candidate = shortened + numbered; } return candidate; }
    private void record(String action,UUID id) { audit.save(AuditEvent.record(UUID.randomUUID(),Instant.now(),actor.required(),action,"BLOG_POST",id,"SUCCESS",null,null,mapper.createObjectNode().put("changedFields","managed"))); }
}
