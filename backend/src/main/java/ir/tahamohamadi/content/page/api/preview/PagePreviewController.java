package ir.tahamohamadi.content.page.api.preview;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.tahamohamadi.common.domain.LanguageCode;
import ir.tahamohamadi.content.page.api.admin.PagePreviewTokenService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/preview/pages")
@ConditionalOnExpression("!'${spring.autoconfigure.exclude:}'.contains('DataSourceAutoConfiguration')")
public class PagePreviewController {
    private final PagePreviewTokenService tokens;
    private final JdbcTemplate jdbc;
    private final ObjectMapper mapper;
    public PagePreviewController(PagePreviewTokenService tokens, JdbcTemplate jdbc, ObjectMapper mapper) { this.tokens = tokens; this.jdbc = jdbc; this.mapper = mapper; }

    @GetMapping("/{pageId}")
    @Transactional(readOnly = true)
    public ResponseEntity<PreviewPageResponse> page(@PathVariable UUID pageId, @RequestParam String token, @RequestParam LanguageCode lang) {
        tokens.requireValid(pageId, token);
        List<PreviewBlock> blocks = jdbc.query("""
                select b.id,b.block_type,b.sort_order,b.is_enabled,section.is_enabled as section_enabled,b.settings_json,t.title,t.eyebrow,t.lead,t.body_markdown,t.action_label,t.action_path,t.alt_text
                from content_page_section section join content_page_block b on b.content_page_section_id=section.id
                join content_page_block_translation t on t.content_page_block_id=b.id and t.language_code=? and t.deleted_at is null
                where section.content_page_id=? and section.deleted_at is null and b.deleted_at is null
                order by section.sort_order,section.id,b.sort_order,b.id
                """, (row, ignored) -> new PreviewBlock(row.getObject("id", UUID.class), row.getString("block_type").toLowerCase().replace('_', '-'), row.getInt("sort_order"), row.getBoolean("is_enabled") && row.getBoolean("section_enabled"), settings(row.getString("settings_json")), row.getString("title"), row.getString("eyebrow"), row.getString("lead"), row.getString("body_markdown"), row.getString("action_label"), row.getString("action_path"), row.getString("alt_text")), lang.name(), pageId);
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).header("X-Robots-Tag", "noindex, nofollow").body(new PreviewPageResponse(lang.name(), blocks));
    }

    private Map<String, Object> settings(String raw) {
        if (raw == null || raw.isBlank()) return Map.of();
        try { return mapper.readValue(raw, new TypeReference<>() { }); }
        catch (Exception ignored) { return Map.of(); }
    }
    public record PreviewPageResponse(String locale, List<PreviewBlock> blocks) { }
    public record PreviewBlock(UUID id, String type, int sortOrder, boolean enabled, Map<String, Object> settings, String title, String eyebrow, String lead, String bodyMarkdown, String actionLabel, String actionPath, String alt) { }
}
