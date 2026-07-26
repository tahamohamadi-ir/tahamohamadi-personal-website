package ir.tahamohamadi.content.page.api.admin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.tahamohamadi.audit.event.AuditEvent;
import ir.tahamohamadi.audit.event.AuditEventRepository;
import ir.tahamohamadi.common.audit.AuthenticatedAuditActor;
import ir.tahamohamadi.common.domain.LanguageCode;
import ir.tahamohamadi.content.page.ContentPage;
import ir.tahamohamadi.content.page.ContentPageRepository;
import ir.tahamohamadi.content.page.ContentPageRevision;
import ir.tahamohamadi.content.page.ContentPageRevisionRepository;
import ir.tahamohamadi.content.page.ContentPageTranslation;
import ir.tahamohamadi.content.page.ContentPageTranslationRepository;
import ir.tahamohamadi.media.asset.MediaAssetRepository;
import ir.tahamohamadi.media.asset.MediaAssetStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class AdminPageRevisionService {
    private final ContentPageRepository pages; private final ContentPageTranslationRepository translations; private final ContentPageRevisionRepository revisions;
    private final JdbcTemplate jdbc; private final ObjectMapper mapper; private final AuthenticatedAuditActor actor; private final AuditEventRepository audit; private final MediaAssetRepository media;
    public AdminPageRevisionService(ContentPageRepository pages, ContentPageTranslationRepository translations, ContentPageRevisionRepository revisions, JdbcTemplate jdbc, ObjectMapper mapper, AuthenticatedAuditActor actor, AuditEventRepository audit, MediaAssetRepository media) {
        this.pages=pages; this.translations=translations; this.revisions=revisions; this.jdbc=jdbc; this.mapper=mapper; this.actor=actor; this.audit=audit; this.media=media;
    }
    @Transactional(readOnly = true) public List<AdminPageRevisionSummary> list(UUID pageId) { page(pageId); return revisions.findByContentPageIdOrderByRevisionNumberDesc(pageId).stream().map(value -> new AdminPageRevisionSummary(value.getId(), value.getRevisionNumber(), value.getReason(), value.getCreatedAt(), value.getCreatedBy())).toList(); }
    @Transactional(readOnly = true) public AdminPageRevisionResponse detail(UUID pageId, UUID revisionId) { ContentPageRevision value=revision(pageId, revisionId); return new AdminPageRevisionResponse(value.getId(), value.getRevisionNumber(), value.getReason(), value.getCreatedAt(), value.getCreatedBy(), decode(value)); }
    public void snapshotBeforeChange(UUID pageId, String reason) {
        ContentPage page = page(pageId);
        if (translations.findByContentPageIdAndLanguageCodeAndDeletedAtIsNull(pageId, LanguageCode.fa).isEmpty()
                || translations.findByContentPageIdAndLanguageCodeAndDeletedAtIsNull(pageId, LanguageCode.en).isEmpty()) return;
        snapshot(page, reason);
    }
    @Transactional public AdminPageResponse restoreAsDraft(UUID pageId, UUID revisionId, long expectedVersion) {
        ContentPage source=page(pageId); version(source, expectedVersion); ContentPageRevision revision=revision(pageId, revisionId); AdminPageRevisionSnapshot snapshot=decode(revision); Instant now=Instant.now();
        ContentPage restored=pages.saveAndFlush(ContentPage.create(UUID.randomUUID(), uniquePageKey(snapshot.page().pageKey(), revision.getRevisionNumber()), now));
        save(restored, LanguageCode.fa, restore(snapshot.page().fa(), LanguageCode.fa, revision.getRevisionNumber()), now);
        save(restored, LanguageCode.en, restore(snapshot.page().en(), LanguageCode.en, revision.getRevisionNumber()), now);
        restoreSections(restored.getId(), snapshot.sections(), now);
        snapshot(restored, "RESTORED_DRAFT");
        audit.save(AuditEvent.record(UUID.randomUUID(), now, actor.required(), "ADMIN_PAGE_REVISION_RESTORED_AS_DRAFT", "PAGE", source.getId(), "SUCCESS", null, null, mapper.createObjectNode().put("restoredPageId", restored.getId().toString()).put("revisionId", revisionId.toString())));
        return response(restored);
    }
    private void snapshot(ContentPage page, String reason) { try { int next=revisions.findFirstByContentPageIdOrderByRevisionNumberDesc(page.getId()).map(value -> value.getRevisionNumber()+1).orElse(1); revisions.save(ContentPageRevision.create(UUID.randomUUID(), page, next, mapper.writeValueAsString(snapshot(page)), reason, Instant.now(), actor.required().getId())); } catch (JsonProcessingException exception) { throw new IllegalStateException("Page revision could not be recorded", exception); } }
    private AdminPageRevisionSnapshot snapshot(ContentPage page) { return new AdminPageRevisionSnapshot(response(page), sections(page.getId())); }
    private AdminPageRevisionSnapshot decode(ContentPageRevision value) { try { return mapper.readValue(value.getSnapshotJson(), AdminPageRevisionSnapshot.class); } catch (JsonProcessingException exception) { throw new IllegalStateException("Stored page revision is invalid", exception); } }
    private ContentPageRevision revision(UUID pageId, UUID revisionId) { return revisions.findByIdAndContentPageId(revisionId,pageId).orElseThrow(() -> new NoSuchElementException("Page revision not found")); }
    private ContentPage page(UUID id) { return pages.findById(id).filter(value -> value.getDeletedAt()==null).orElseThrow(() -> new NoSuchElementException("Page not found")); }
    private AdminPageResponse response(ContentPage page) { return new AdminPageResponse(page.getId(),page.getPageKey(),page.getStatus().name(),page.getPublishedAt(),translation(page,LanguageCode.fa),translation(page,LanguageCode.en),page.getVersion()); }
    private PageTranslationRequest translation(ContentPage page, LanguageCode language) { ContentPageTranslation value=translations.findByContentPageIdAndLanguageCodeAndDeletedAtIsNull(page.getId(),language).orElseThrow(() -> new NoSuchElementException("Page translation not found")); return new PageTranslationRequest(value.getTitle(),value.getSlug(),value.getSummary(),value.getBodyMarkdown(),value.getSeoTitle(),value.getSeoDescription(),value.getCanonicalPath()); }
    private void save(ContentPage page, LanguageCode language, PageTranslationRequest value, Instant now) { ContentPageTranslation translation=ContentPageTranslation.create(UUID.randomUUID(),page,language,value.title(),value.slug(),now); translation.update(value.title(),value.slug(),value.summary(),value.bodyMarkdown(),value.seoTitle(),value.seoDescription(),value.canonicalPath()); translations.save(translation); }
    private PageTranslationRequest restore(PageTranslationRequest value, LanguageCode language, int revisionNumber) { String suffix="-revision-"+revisionNumber; return new PageTranslationRequest(value.title(),uniqueSlug(value.slug(),language,suffix),value.summary(),value.bodyMarkdown(),value.seoTitle(),value.seoDescription(),value.canonicalPath()); }
    private String uniquePageKey(String source, int revisionNumber) { String candidate=source+"-revision-"+revisionNumber; int attempt=2; while(pages.findByPageKey(candidate).isPresent()) candidate=source+"-revision-"+revisionNumber+"-"+attempt++; return candidate; }
    private String uniqueSlug(String source, LanguageCode language, String suffix) { String candidate=source+suffix; int attempt=2; while(translations.existsByLanguageCodeAndSlugIgnoreCaseAndDeletedAtIsNull(language,candidate)) candidate=source+suffix+"-"+attempt++; return candidate; }
    private List<PageSectionSnapshot> sections(UUID pageId) { return jdbc.query("select id,section_type,layout,is_enabled,settings_json from content_page_section where content_page_id=? and deleted_at is null order by sort_order,id", (rs,row) -> new PageSectionSnapshot(rs.getString("section_type"),rs.getString("layout"),rs.getBoolean("is_enabled"),rs.getString("settings_json"),blocks(rs.getObject("id",UUID.class))),pageId); }
    private List<PageBlockSnapshot> blocks(UUID sectionId) { return jdbc.query("select b.block_type,b.is_enabled,b.settings_json,fa.title fa_title,fa.eyebrow fa_eyebrow,fa.lead fa_lead,fa.body_markdown fa_body,fa.action_label fa_action_label,fa.action_path fa_action_path,fa.alt_text fa_alt,en.title en_title,en.eyebrow en_eyebrow,en.lead en_lead,en.body_markdown en_body,en.action_label en_action_label,en.action_path en_action_path,en.alt_text en_alt from content_page_block b join content_page_block_translation fa on fa.content_page_block_id=b.id and fa.language_code='fa' and fa.deleted_at is null join content_page_block_translation en on en.content_page_block_id=b.id and en.language_code='en' and en.deleted_at is null where b.content_page_section_id=? and b.deleted_at is null order by b.sort_order,b.id", (rs,row) -> new PageBlockSnapshot(rs.getString("block_type"),rs.getBoolean("is_enabled"),rs.getString("settings_json"),translation(rs,"fa"),translation(rs,"en")),sectionId); }
    private static BlockTranslationSnapshot translation(java.sql.ResultSet rs, String locale) throws java.sql.SQLException { return new BlockTranslationSnapshot(rs.getString(locale+"_title"),rs.getString(locale+"_eyebrow"),rs.getString(locale+"_lead"),rs.getString(locale+"_body"),rs.getString(locale+"_action_label"),rs.getString(locale+"_action_path"),rs.getString(locale+"_alt")); }
    private void restoreSections(UUID pageId, List<PageSectionSnapshot> sections, Instant now) { for(int sectionIndex=0;sectionIndex<sections.size();sectionIndex++) { PageSectionSnapshot section=sections.get(sectionIndex); UUID sectionId=UUID.randomUUID(); jdbc.update("insert into content_page_section (id,content_page_id,section_type,layout,sort_order,is_enabled,settings_json,created_at,updated_at,version) values (?,?,?,?,?,?,?,?,?,0)",sectionId,pageId,section.type().trim().toUpperCase(Locale.ROOT),section.layout().trim().toUpperCase(Locale.ROOT),sectionIndex,section.enabled(),section.settingsJson(),timestamp(now),timestamp(now)); for(int blockIndex=0;blockIndex<section.blocks().size();blockIndex++) restoreBlock(pageId,sectionId,blockIndex,section.blocks().get(blockIndex),now); } }
    private void restoreBlock(UUID pageId, UUID sectionId, int index, PageBlockSnapshot block, Instant now) { validateMedia(block.settingsJson()); UUID blockId=UUID.randomUUID(); jdbc.update("insert into content_page_block (id,content_page_id,content_page_section_id,block_type,sort_order,is_enabled,settings_json,created_at,updated_at,version) values (?,?,?,?,?,?,?,?,?,0)",blockId,pageId,sectionId,block.type().trim().toUpperCase(Locale.ROOT),index,block.enabled(),block.settingsJson(),timestamp(now),timestamp(now)); insertTranslation(blockId,"fa",block.fa(),now); insertTranslation(blockId,"en",block.en(),now); }
    private void validateMedia(String settingsJson) { if(settingsJson==null || settingsJson.isBlank()) return; try { com.fasterxml.jackson.databind.JsonNode value=mapper.readTree(settingsJson); if(value.has("mediaId")) { UUID id=UUID.fromString(value.get("mediaId").asText()); if(media.findByIdAndStatusAndDeletedAtIsNull(id, MediaAssetStatus.ACTIVE).isEmpty()) throw new IllegalStateException("Page revision references an unavailable media asset"); } } catch(JsonProcessingException|IllegalArgumentException exception) { throw new IllegalStateException("Stored page revision contains invalid block settings",exception); } }
    private void insertTranslation(UUID blockId,String locale,BlockTranslationSnapshot value,Instant now) { jdbc.update("insert into content_page_block_translation (id,content_page_block_id,language_code,title,eyebrow,lead,body_markdown,action_label,action_path,alt_text,created_at,updated_at,version) values (?,?,?,?,?,?,?,?,?,?,?,?,0)",UUID.randomUUID(),blockId,locale,value.title(),value.eyebrow(),value.lead(),value.bodyMarkdown(),value.actionLabel(),value.actionPath(),value.alt(),timestamp(now),timestamp(now)); }
    private static Timestamp timestamp(Instant value) { return Timestamp.from(value); }
    private static void version(ContentPage page,long expected) { if(page.getVersion()!=expected) throw new ObjectOptimisticLockingFailureException(ContentPage.class,page.getId()); }
}
