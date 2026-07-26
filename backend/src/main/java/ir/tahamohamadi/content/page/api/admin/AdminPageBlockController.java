package ir.tahamohamadi.content.page.api.admin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.tahamohamadi.audit.event.AuditEvent;
import ir.tahamohamadi.audit.event.AuditEventRepository;
import ir.tahamohamadi.common.audit.AuthenticatedAuditActor;
import ir.tahamohamadi.content.page.ContentPage;
import ir.tahamohamadi.media.asset.MediaAssetRepository;
import ir.tahamohamadi.media.asset.MediaAssetStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.sql.Timestamp;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * A deliberately typed page-composer boundary. It stores only the approved
 * block catalogue and localized text; public rendering never evaluates HTML,
 * CSS, script, or arbitrary component names from this resource.
 */
@RestController
@RequestMapping("/api/v1/admin/pages/{pageId}/blocks")
@ConditionalOnExpression("!'${spring.autoconfigure.exclude:}'.contains('DataSourceAutoConfiguration')")
public class AdminPageBlockController {
    private static final Set<String> TYPES = Set.of("HERO", "RICH_TEXT", "MEDIA", "MEDIA_TEXT", "CALL_TO_ACTION", "COLLECTION", "SKILLS", "RESUME", "SOCIAL_LINKS", "CONTACT");
    private final JdbcTemplate jdbc;
    private final ObjectMapper objectMapper;
    private final AuditEventRepository audit;
    private final AuthenticatedAuditActor actor;
    private final MediaAssetRepository mediaAssets;

    public AdminPageBlockController(JdbcTemplate jdbc, ObjectMapper objectMapper, AuditEventRepository audit, AuthenticatedAuditActor actor, MediaAssetRepository mediaAssets) {
        this.jdbc = jdbc;
        this.objectMapper = objectMapper;
        this.audit = audit;
        this.actor = actor;
        this.mediaAssets = mediaAssets;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public PageBlocksResponse get(@PathVariable UUID pageId) {
        long version = version(pageId);
        return new PageBlocksResponse(version, blocks(pageId));
    }

    @GetMapping("/composition")
    @Transactional(readOnly = true)
    public PageCompositionResponse composition(@PathVariable UUID pageId) {
        return new PageCompositionResponse(version(pageId), sections(pageId));
    }

    @PutMapping
    @Transactional
    public PageBlocksResponse replace(@PathVariable UUID pageId, @Valid @RequestBody PageBlocksRequest request) {
        long current = version(pageId);
        if (current != request.version()) throw new ObjectOptimisticLockingFailureException(ContentPage.class, pageId);
        Instant now = Instant.now();
        replaceSections(pageId, List.of(new PageSectionRequest("STANDARD", "SINGLE_COLUMN", true, null, request.blocks())), now);
        long updated = updatePageVersion(pageId, current, now);
        audit.save(AuditEvent.record(UUID.randomUUID(), now, actor.required(), "ADMIN_PAGE_BLOCKS_UPDATED", "PAGE", pageId, "SUCCESS", null, null, objectMapper.createObjectNode().put("changedFields", "blocks")));
        return new PageBlocksResponse(updated, blocks(pageId));
    }

    @PutMapping("/composition")
    @Transactional
    public PageCompositionResponse replaceComposition(@PathVariable UUID pageId, @Valid @RequestBody PageCompositionRequest request) {
        long current = version(pageId);
        if (current != request.version()) throw new ObjectOptimisticLockingFailureException(ContentPage.class, pageId);
        Instant now = Instant.now();
        replaceSections(pageId, request.sections(), now);
        long updated = updatePageVersion(pageId, current, now);
        audit.save(AuditEvent.record(UUID.randomUUID(), now, actor.required(), "ADMIN_PAGE_COMPOSITION_UPDATED", "PAGE", pageId, "SUCCESS", null, null, objectMapper.createObjectNode().put("changedFields", "sections")));
        return new PageCompositionResponse(updated, sections(pageId));
    }

    private void insertTranslation(UUID blockId, String locale, BlockTranslationRequest value, Instant now) {
        if (value.actionPath() != null && !value.actionPath().isBlank() && !(value.actionPath().matches("^/(fa|en)(/.*)?$") || value.actionPath().startsWith("https://"))) {
            throw new IllegalArgumentException("Block action path must be an approved internal path or HTTPS URL");
        }
        jdbc.update("insert into content_page_block_translation (id,content_page_block_id,language_code,title,eyebrow,lead,body_markdown,action_label,action_path,alt_text,created_at,updated_at,version) values (?,?,?,?,?,?,?,?,?,?,?,?,0)", UUID.randomUUID(), blockId, locale, value.title(), value.eyebrow(), value.lead(), value.bodyMarkdown(), value.actionLabel(), value.actionPath(), value.alt(), timestamp(now), timestamp(now));
    }

    private void replaceSections(UUID pageId, List<PageSectionRequest> sections, Instant now) {
        jdbc.update("delete from content_page_section where content_page_id=?", pageId);
        for (int sectionIndex = 0; sectionIndex < sections.size(); sectionIndex++) {
            PageSectionRequest section = sections.get(sectionIndex);
            validateSection(section);
            UUID sectionId = UUID.randomUUID();
            jdbc.update("insert into content_page_section (id,content_page_id,section_type,layout,sort_order,is_enabled,settings_json,created_at,updated_at,version) values (?,?,?,?,?,?,?,?,?,0)", sectionId, pageId, section.type().trim().toUpperCase(Locale.ROOT), section.layout().trim().toUpperCase(Locale.ROOT), sectionIndex, section.enabled(), section.settingsJson(), timestamp(now), timestamp(now));
            for (int blockIndex = 0; blockIndex < section.blocks().size(); blockIndex++) insertBlock(pageId, sectionId, blockIndex, section.blocks().get(blockIndex), now);
        }
    }

    private void insertBlock(UUID pageId, UUID sectionId, int index, PageBlockRequest block, Instant now) {
        String type = block.type().trim().toUpperCase(Locale.ROOT);
        if (!TYPES.contains(type)) throw new IllegalArgumentException("Unsupported page block type");
        validateSettings(type, block.settingsJson());
        UUID id = UUID.randomUUID();
        jdbc.update("insert into content_page_block (id,content_page_id,content_page_section_id,block_type,sort_order,is_enabled,settings_json,created_at,updated_at,version) values (?,?,?,?,?,?,?,?,?,0)", id, pageId, sectionId, type, index, block.enabled(), block.settingsJson(), timestamp(now), timestamp(now));
        insertTranslation(id, "fa", block.fa(), now);
        insertTranslation(id, "en", block.en(), now);
    }

    private void validateSection(PageSectionRequest section) {
        if (!"STANDARD".equals(section.type().trim().toUpperCase(Locale.ROOT)) || !"SINGLE_COLUMN".equals(section.layout().trim().toUpperCase(Locale.ROOT))) throw new IllegalArgumentException("Unsupported page section");
        if (section.settingsJson() != null && !section.settingsJson().isBlank()) {
            try {
                if (!objectMapper.readTree(section.settingsJson()).isObject()) throw new IllegalArgumentException("Section settings must be a JSON object");
            } catch (com.fasterxml.jackson.core.JsonProcessingException exception) { throw new IllegalArgumentException("Section settings must be valid JSON", exception); }
        }
    }

    private long updatePageVersion(UUID pageId, long current, Instant now) {
        int changed = jdbc.update("update content_page set updated_at=?, version=version+1 where id=? and deleted_at is null and version=?", timestamp(now), pageId, current);
        if (changed != 1) throw new ObjectOptimisticLockingFailureException(ContentPage.class, pageId);
        return version(pageId);
    }

    private long version(UUID pageId) {
        List<Long> result = jdbc.query("select version from content_page where id=? and deleted_at is null", (rs, row) -> rs.getLong(1), pageId);
        if (result.isEmpty()) throw new java.util.NoSuchElementException("Page not found");
        return result.getFirst();
    }

    private void validateSettings(String type, String settingsJson) {
        if (settingsJson == null || settingsJson.isBlank()) return;
        try {
            JsonNode value = objectMapper.readTree(settingsJson);
            if (value == null || !value.isObject()) {
                throw new IllegalArgumentException("Block settings must be a JSON object");
            }
            Set<String> allowed = switch (type) {
                case "HERO", "MEDIA", "MEDIA_TEXT" -> Set.of("mediaId");
                case "COLLECTION" -> Set.of("source", "limit");
                default -> Set.of();
            };
            value.fieldNames().forEachRemaining(field -> {
                if (!allowed.contains(field)) throw new IllegalArgumentException("Unsupported settings field for block type");
            });
            if (value.has("mediaId") && !value.get("mediaId").isTextual()) {
                throw new IllegalArgumentException("Media setting must be an asset identifier");
            }
            if (value.has("mediaId")) {
                UUID mediaId;
                try {
                    mediaId = UUID.fromString(value.get("mediaId").asText());
                } catch (IllegalArgumentException exception) {
                    throw new IllegalArgumentException("Media setting must be an asset identifier", exception);
                }
                if (mediaAssets.findByIdAndStatusAndDeletedAtIsNull(mediaId, MediaAssetStatus.ACTIVE).isEmpty()) {
                    throw new IllegalArgumentException("Media setting must reference an active asset");
                }
            }
            if (value.has("source") && (!value.get("source").isTextual() || !Set.of("BLOG", "PORTFOLIO", "PUBLICATIONS").contains(value.get("source").asText()))) {
                throw new IllegalArgumentException("Collection source is not supported");
            }
            if (value.has("limit") && (!value.get("limit").canConvertToInt() || value.get("limit").asInt() < 1 || value.get("limit").asInt() > 12)) {
                throw new IllegalArgumentException("Collection limit must be between 1 and 12");
            }
        } catch (com.fasterxml.jackson.core.JsonProcessingException exception) {
            throw new IllegalArgumentException("Block settings must be valid JSON", exception);
        }
    }

    private List<PageBlockResponse> blocks(UUID pageId) {
        return jdbc.query("select b.id,b.block_type,b.sort_order,b.is_enabled,b.settings_json,fa.title fa_title,fa.eyebrow fa_eyebrow,fa.lead fa_lead,fa.body_markdown fa_body,fa.action_label fa_action_label,fa.action_path fa_action_path,fa.alt_text fa_alt,en.title en_title,en.eyebrow en_eyebrow,en.lead en_lead,en.body_markdown en_body,en.action_label en_action_label,en.action_path en_action_path,en.alt_text en_alt from content_page_block b join content_page_block_translation fa on fa.content_page_block_id=b.id and fa.language_code='fa' and fa.deleted_at is null join content_page_block_translation en on en.content_page_block_id=b.id and en.language_code='en' and en.deleted_at is null where b.content_page_id=? and b.deleted_at is null order by b.sort_order,b.id", (rs, row) -> new PageBlockResponse(rs.getObject("id", UUID.class), rs.getString("block_type"), rs.getInt("sort_order"), rs.getBoolean("is_enabled"), rs.getString("settings_json"), translation(rs, "fa"), translation(rs, "en")), pageId);
    }

    private List<PageSectionResponse> sections(UUID pageId) {
        return jdbc.query("select id,section_type,layout,sort_order,is_enabled,settings_json from content_page_section where content_page_id=? and deleted_at is null order by sort_order,id", (rs, row) -> {
            UUID sectionId = rs.getObject("id", UUID.class);
            return new PageSectionResponse(sectionId, rs.getString("section_type"), rs.getString("layout"), rs.getInt("sort_order"), rs.getBoolean("is_enabled"), rs.getString("settings_json"), blocksForSection(sectionId));
        }, pageId);
    }

    private List<PageBlockResponse> blocksForSection(UUID sectionId) {
        return jdbc.query("select b.id,b.block_type,b.sort_order,b.is_enabled,b.settings_json,fa.title fa_title,fa.eyebrow fa_eyebrow,fa.lead fa_lead,fa.body_markdown fa_body,fa.action_label fa_action_label,fa.action_path fa_action_path,fa.alt_text fa_alt,en.title en_title,en.eyebrow en_eyebrow,en.lead en_lead,en.body_markdown en_body,en.action_label en_action_label,en.action_path en_action_path,en.alt_text en_alt from content_page_block b join content_page_block_translation fa on fa.content_page_block_id=b.id and fa.language_code='fa' and fa.deleted_at is null join content_page_block_translation en on en.content_page_block_id=b.id and en.language_code='en' and en.deleted_at is null where b.content_page_section_id=? and b.deleted_at is null order by b.sort_order,b.id", (rs, row) -> new PageBlockResponse(rs.getObject("id", UUID.class), rs.getString("block_type"), rs.getInt("sort_order"), rs.getBoolean("is_enabled"), rs.getString("settings_json"), translation(rs, "fa"), translation(rs, "en")), sectionId);
    }

    private static BlockTranslationResponse translation(java.sql.ResultSet rs, String locale) throws java.sql.SQLException {
        return new BlockTranslationResponse(rs.getString(locale + "_title"), rs.getString(locale + "_eyebrow"), rs.getString(locale + "_lead"), rs.getString(locale + "_body"), rs.getString(locale + "_action_label"), rs.getString(locale + "_action_path"), rs.getString(locale + "_alt"));
    }

    private static Timestamp timestamp(Instant value) { return Timestamp.from(value); }

    public record PageBlocksRequest(@NotNull @Min(0) Long version, @NotNull @Size(max = 50) List<@Valid PageBlockRequest> blocks) { }
    public record PageCompositionRequest(@NotNull @Min(0) Long version, @NotNull @Size(min = 1, max = 20) List<@Valid PageSectionRequest> sections) { }
    public record PageSectionRequest(@NotBlank @Size(max = 64) String type, @NotBlank @Size(max = 64) String layout, boolean enabled, @Size(max = 10000) String settingsJson, @NotNull @Size(max = 50) List<@Valid PageBlockRequest> blocks) { }
    public record PageBlockRequest(@NotBlank @Size(max = 64) String type, boolean enabled, @Size(max = 10000) String settingsJson, @Valid @NotNull BlockTranslationRequest fa, @Valid @NotNull BlockTranslationRequest en) { }
    public record BlockTranslationRequest(@Size(max = 255) String title, @Size(max = 255) String eyebrow, @Size(max = 10000) String lead, @Size(max = 100000) String bodyMarkdown, @Size(max = 255) String actionLabel, @Size(max = 2048) String actionPath, @Size(max = 500) String alt) { }
    public record PageBlocksResponse(long version, List<PageBlockResponse> blocks) { }
    public record PageCompositionResponse(long version, List<PageSectionResponse> sections) { }
    public record PageSectionResponse(UUID id, String type, String layout, int sortOrder, boolean enabled, String settingsJson, List<PageBlockResponse> blocks) { }
    public record PageBlockResponse(UUID id, String type, int sortOrder, boolean enabled, String settingsJson, BlockTranslationResponse fa, BlockTranslationResponse en) { }
    public record BlockTranslationResponse(String title, String eyebrow, String lead, String bodyMarkdown, String actionLabel, String actionPath, String alt) { }
}
