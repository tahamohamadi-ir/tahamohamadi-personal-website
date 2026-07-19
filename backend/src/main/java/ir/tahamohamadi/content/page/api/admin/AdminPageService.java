package ir.tahamohamadi.content.page.api.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.tahamohamadi.audit.event.AuditEvent;
import ir.tahamohamadi.audit.event.AuditEventRepository;
import ir.tahamohamadi.common.domain.LanguageCode;
import ir.tahamohamadi.common.audit.AuthenticatedAuditActor;
import ir.tahamohamadi.content.page.ContentPage;
import ir.tahamohamadi.content.page.ContentPageRepository;
import ir.tahamohamadi.content.page.ContentPageTranslation;
import ir.tahamohamadi.content.page.ContentPageTranslationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@ConditionalOnExpression("!'${spring.autoconfigure.exclude:}'.contains('DataSourceAutoConfiguration')")
public class AdminPageService {
    private final ContentPageRepository pages; private final ContentPageTranslationRepository translations;
    private final AuditEventRepository audit; private final ObjectMapper mapper; private final AuthenticatedAuditActor actor;
    public AdminPageService(ContentPageRepository pages, ContentPageTranslationRepository translations, AuditEventRepository audit, ObjectMapper mapper, AuthenticatedAuditActor actor) { this.pages=pages; this.translations=translations; this.audit=audit; this.mapper=mapper; this.actor=actor; }
    @Transactional(readOnly=true) public Page<AdminPageResponse> list(Pageable pageable) {
        Page<ContentPage> result = pages.findByDeletedAtIsNullOrderByUpdatedAtDescIdDesc(pageable);
        Map<UUID, List<ContentPageTranslation>> localized = result.getContent().isEmpty() ? Map.of() : translations.findByContentPageIdInAndDeletedAtIsNull(ids(result.getContent())).stream().collect(Collectors.groupingBy(value -> value.getContentPage().getId()));
        return result.map(page -> response(page, localized.get(page.getId())));
    }
    @Transactional(readOnly=true) public AdminPageResponse get(UUID id) { return response(page(id)); }
    @Transactional public AdminPageResponse create(AdminPageRequest request) { ContentPage page=pages.save(ContentPage.create(UUID.randomUUID(),request.pageKey(),Instant.now())); saveTranslations(page,request); record("ADMIN_PAGE_CREATED",page.getId()); return response(page); }
    @Transactional public AdminPageResponse update(UUID id,AdminPageRequest request) { ContentPage page=page(id); version(page,request.version()); page.rename(request.pageKey()); saveTranslations(page,request); record("ADMIN_PAGE_UPDATED",id); return response(page); }
    @Transactional public AdminPageResponse publish(UUID id, long version) { ContentPage page=page(id); version(page,version); requirePublishable(page); page.publish(Instant.now()); record("ADMIN_PAGE_PUBLISHED",id); return response(page); }
    @Transactional public AdminPageResponse archive(UUID id,long version) { ContentPage page=page(id); version(page,version); page.archive(); record("ADMIN_PAGE_ARCHIVED",id); return response(page); }
    @Transactional public void delete(UUID id,long version) { ContentPage page=page(id); version(page,version); page.softDelete(actor.required(),Instant.now()); record("ADMIN_PAGE_DELETED",id); }
    private ContentPage page(UUID id) { return pages.findById(id).filter(p->p.getDeletedAt()==null).orElseThrow(()->new java.util.NoSuchElementException("Page not found")); }
    private void saveTranslations(ContentPage page,AdminPageRequest request) { save(page,LanguageCode.fa,request.fa()); save(page,LanguageCode.en,request.en()); }
    private void save(ContentPage page,LanguageCode language,PageTranslationRequest request) { translations.findByContentPageIdAndLanguageCodeAndDeletedAtIsNull(page.getId(),language).ifPresentOrElse(t->t.update(request.title(),request.slug(),request.summary(),request.bodyMarkdown(),request.seoTitle(),request.seoDescription(),request.canonicalPath()),()->{ ContentPageTranslation t=ContentPageTranslation.create(UUID.randomUUID(),page,language,request.title(),request.slug(),Instant.now()); t.update(request.title(),request.slug(),request.summary(),request.bodyMarkdown(),request.seoTitle(),request.seoDescription(),request.canonicalPath()); translations.save(t); }); }
    private AdminPageResponse response(ContentPage page) {
        List<ContentPageTranslation> values = translations.findByContentPageIdInAndDeletedAtIsNull(List.of(page.getId()));
        return response(page, values);
    }
    private AdminPageResponse response(ContentPage page, List<ContentPageTranslation> values) { return new AdminPageResponse(page.getId(),page.getPageKey(),page.getStatus().name(),page.getPublishedAt(),translation(values,LanguageCode.fa),translation(values,LanguageCode.en),page.getVersion()); }
    private static List<UUID> ids(Collection<ContentPage> pages) { return pages.stream().map(ContentPage::getId).toList(); }
    private PageTranslationRequest translation(List<ContentPageTranslation> values, LanguageCode language) { ContentPageTranslation t=values.stream().filter(value -> value.getLanguageCode()==language).findFirst().orElseThrow(()->new java.util.NoSuchElementException("Page translation not found")); return new PageTranslationRequest(t.getTitle(),t.getSlug(),t.getSummary(),t.getBodyMarkdown(),t.getSeoTitle(),t.getSeoDescription(),t.getCanonicalPath()); }
    private PageTranslationRequest translation(ContentPage page,LanguageCode language) { ContentPageTranslation t=translations.findByContentPageIdAndLanguageCodeAndDeletedAtIsNull(page.getId(),language).orElseThrow(()->new java.util.NoSuchElementException("Page translation not found")); return new PageTranslationRequest(t.getTitle(),t.getSlug(),t.getSummary(),t.getBodyMarkdown(),t.getSeoTitle(),t.getSeoDescription(),t.getCanonicalPath()); }
    private void requirePublishable(ContentPage page) { for(LanguageCode lang:LanguageCode.values()) { PageTranslationRequest t=translation(page,lang); if(t.seoTitle()==null||t.seoTitle().isBlank()||t.seoDescription()==null||t.seoDescription().isBlank()) throw new IllegalStateException("Publishing requires localized SEO metadata"); } }
    private static void version(ContentPage page,Long version) { if(version==null||page.getVersion()!=version) throw new ObjectOptimisticLockingFailureException(ContentPage.class,page.getId()); }
    private void record(String action,UUID id) { audit.save(AuditEvent.record(UUID.randomUUID(),Instant.now(),actor.required(),action,"PAGE",id,"SUCCESS",null,null,mapper.createObjectNode().put("changedFields","managed"))); }
}
