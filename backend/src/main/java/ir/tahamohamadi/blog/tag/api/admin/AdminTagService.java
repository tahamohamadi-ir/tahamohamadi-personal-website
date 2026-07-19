package ir.tahamohamadi.blog.tag.api.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.tahamohamadi.audit.event.AuditEvent;
import ir.tahamohamadi.audit.event.AuditEventRepository;
import ir.tahamohamadi.blog.tag.Tag;
import ir.tahamohamadi.blog.tag.TagRepository;
import ir.tahamohamadi.blog.tag.TagTranslation;
import ir.tahamohamadi.blog.tag.TagTranslationRepository;
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
public class AdminTagService {
    private final TagRepository tags; private final TagTranslationRepository translations;
    private final AuditEventRepository audit; private final ObjectMapper mapper; private final AuthenticatedAuditActor actor;
    public AdminTagService(TagRepository tags, TagTranslationRepository translations, AuditEventRepository audit, ObjectMapper mapper, AuthenticatedAuditActor actor) { this.tags=tags; this.translations=translations; this.audit=audit; this.mapper=mapper; this.actor=actor; }
    @Transactional(readOnly=true) public Page<AdminTagResponse> list(Pageable pageable) { Page<Tag> page=tags.findByDeletedAtIsNullOrderByTagKeyAscIdAsc(pageable); Map<UUID,List<TagTranslation>> values=translations.findByTagIdInAndDeletedAtIsNull(page.getContent().stream().map(Tag::getId).toList()).stream().collect(java.util.stream.Collectors.groupingBy(value->value.getTag().getId())); return new PageImpl<>(page.getContent().stream().map(value->response(value,values.get(value.getId()))).toList(),pageable,page.getTotalElements()); }
    @Transactional(readOnly=true) public AdminTagResponse get(UUID id) { return response(tag(id)); }
    @Transactional public AdminTagResponse create(AdminTagRequest request) { Tag tag=tags.save(Tag.create(UUID.randomUUID(),request.tagKey(),Instant.now())); saveTranslations(tag,request); tags.flush(); record("ADMIN_TAG_CREATED",tag.getId()); return response(tag); }
    @Transactional public AdminTagResponse update(UUID id,AdminTagRequest request) { Tag tag=tag(id); version(tag,request.version()); tag.rename(request.tagKey()); saveTranslations(tag,request); tags.flush(); record("ADMIN_TAG_UPDATED",id); return response(tag); }
    @Transactional public void deactivate(UUID id,long version) { Tag tag=tag(id); version(tag,version); tag.deactivate(); tags.flush(); record("ADMIN_TAG_DEACTIVATED",id); }
    private Tag tag(UUID id) { return tags.findById(id).filter(value->value.getDeletedAt()==null).orElseThrow(()->new NoSuchElementException("Tag not found")); }
    private void saveTranslations(Tag tag,AdminTagRequest request) { save(tag,LanguageCode.fa,request.fa()); save(tag,LanguageCode.en,request.en()); }
    private void save(Tag tag,LanguageCode language,AdminTagTranslationRequest request) { translations.findByTagIdAndLanguageCodeAndDeletedAtIsNull(tag.getId(),language).ifPresentOrElse(value->value.update(request.name(),request.slug(),request.seoTitle(),request.seoDescription()),()->{ TagTranslation value=TagTranslation.create(UUID.randomUUID(),tag,language,request.name(),request.slug(),Instant.now()); value.update(request.name(),request.slug(),request.seoTitle(),request.seoDescription()); translations.save(value); }); }
    private AdminTagResponse response(Tag tag) { return response(tag,translations.findByTagIdAndDeletedAtIsNull(tag.getId())); }
    private AdminTagResponse response(Tag tag,List<TagTranslation> values) { return new AdminTagResponse(tag.getId(),tag.getTagKey(),tag.isActive(),translation(values,LanguageCode.fa),translation(values,LanguageCode.en),tag.getVersion()); }
    private AdminTagTranslationRequest translation(List<TagTranslation> values,LanguageCode language) { TagTranslation value=values.stream().filter(item->item.getLanguageCode()==language).findFirst().orElseThrow(()->new NoSuchElementException("Tag translation not found")); return new AdminTagTranslationRequest(value.getName(),value.getSlug(),value.getSeoTitle(),value.getSeoDescription()); }
    private static void version(Tag value,Long version) { if(version==null||value.getVersion()!=version) throw new ObjectOptimisticLockingFailureException(Tag.class,value.getId()); }
    private void record(String action,UUID id) { audit.save(AuditEvent.record(UUID.randomUUID(),Instant.now(),actor.required(),action,"TAG",id,"SUCCESS",null,null,mapper.createObjectNode().put("changedFields","managed"))); }
}
