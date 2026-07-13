package ir.tahamohamadi.blog.category.api.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.tahamohamadi.audit.event.AuditEvent;
import ir.tahamohamadi.audit.event.AuditEventRepository;
import ir.tahamohamadi.blog.category.BlogCategory;
import ir.tahamohamadi.blog.category.BlogCategoryRepository;
import ir.tahamohamadi.blog.category.BlogCategoryTranslation;
import ir.tahamohamadi.blog.category.BlogCategoryTranslationRepository;
import ir.tahamohamadi.common.audit.AuthenticatedAuditActor;
import ir.tahamohamadi.common.domain.LanguageCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class AdminBlogCategoryService {
    private final BlogCategoryRepository categories; private final BlogCategoryTranslationRepository translations;
    private final AuditEventRepository audit; private final ObjectMapper mapper; private final AuthenticatedAuditActor actor;
    public AdminBlogCategoryService(BlogCategoryRepository categories, BlogCategoryTranslationRepository translations, AuditEventRepository audit, ObjectMapper mapper, AuthenticatedAuditActor actor) { this.categories=categories; this.translations=translations; this.audit=audit; this.mapper=mapper; this.actor=actor; }
    @Transactional(readOnly=true) public Page<AdminBlogCategoryResponse> list(Pageable pageable) { Page<BlogCategory> page=categories.findByDeletedAtIsNullOrderBySortOrderAscIdAsc(pageable); Map<UUID,List<BlogCategoryTranslation>> values=translations.findByCategoryIdInAndDeletedAtIsNull(page.getContent().stream().map(BlogCategory::getId).toList()).stream().collect(java.util.stream.Collectors.groupingBy(value->value.getCategory().getId())); return new PageImpl<>(page.getContent().stream().map(value->response(value,values.get(value.getId()))).toList(),pageable,page.getTotalElements()); }
    @Transactional(readOnly=true) public AdminBlogCategoryResponse get(UUID id) { return response(category(id)); }
    @Transactional public AdminBlogCategoryResponse create(AdminBlogCategoryRequest request) { BlogCategory category=categories.save(BlogCategory.create(UUID.randomUUID(),request.categoryKey(),request.sortOrder(),Instant.now())); saveTranslations(category,request); categories.flush(); record("ADMIN_BLOG_CATEGORY_CREATED",category.getId()); return response(category); }
    @Transactional public AdminBlogCategoryResponse update(UUID id,AdminBlogCategoryRequest request) { BlogCategory category=category(id); version(category,request.version()); category.update(request.categoryKey(),request.sortOrder()); saveTranslations(category,request); categories.flush(); record("ADMIN_BLOG_CATEGORY_UPDATED",id); return response(category); }
    @Transactional public void deactivate(UUID id,long version) { BlogCategory category=category(id); version(category,version); category.deactivate(); categories.flush(); record("ADMIN_BLOG_CATEGORY_DEACTIVATED",id); }
    private BlogCategory category(UUID id) { return categories.findById(id).filter(value->value.getDeletedAt()==null).orElseThrow(()->new NoSuchElementException("Category not found")); }
    private void saveTranslations(BlogCategory category,AdminBlogCategoryRequest request) { save(category,LanguageCode.fa,request.fa()); save(category,LanguageCode.en,request.en()); }
    private void save(BlogCategory category,LanguageCode language,AdminCategoryTranslationRequest request) { translations.findByCategoryIdAndLanguageCodeAndDeletedAtIsNull(category.getId(),language).ifPresentOrElse(value->value.update(request.name(),request.slug(),request.seoTitle(),request.seoDescription()),()->{ BlogCategoryTranslation value=BlogCategoryTranslation.create(UUID.randomUUID(),category,language,request.name(),request.slug(),Instant.now()); value.update(request.name(),request.slug(),request.seoTitle(),request.seoDescription()); translations.save(value); }); }
    private AdminBlogCategoryResponse response(BlogCategory category) { return response(category,translations.findByCategoryIdAndDeletedAtIsNull(category.getId())); }
    private AdminBlogCategoryResponse response(BlogCategory category,List<BlogCategoryTranslation> values) { return new AdminBlogCategoryResponse(category.getId(),category.getCategoryKey(),category.getSortOrder(),category.isActive(),translation(values,LanguageCode.fa),translation(values,LanguageCode.en),category.getVersion()); }
    private AdminCategoryTranslationRequest translation(List<BlogCategoryTranslation> values,LanguageCode language) { BlogCategoryTranslation value=values.stream().filter(item->item.getLanguageCode()==language).findFirst().orElseThrow(()->new NoSuchElementException("Category translation not found")); return new AdminCategoryTranslationRequest(value.getName(),value.getSlug(),value.getSeoTitle(),value.getSeoDescription()); }
    private static void version(BlogCategory value,Long version) { if(version==null||value.getVersion()!=version) throw new ObjectOptimisticLockingFailureException(BlogCategory.class,value.getId()); }
    private void record(String action,UUID id) { audit.save(AuditEvent.record(UUID.randomUUID(),Instant.now(),actor.required(),action,"BLOG_CATEGORY",id,"SUCCESS",null,null,mapper.createObjectNode().put("changedFields","managed"))); }
}
