package ir.tahamohamadi.content.site.api.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.tahamohamadi.audit.event.AuditEvent;
import ir.tahamohamadi.audit.event.AuditEventRepository;
import ir.tahamohamadi.common.audit.AuthenticatedAuditActor;
import ir.tahamohamadi.media.asset.MediaAssetRepository;
import ir.tahamohamadi.media.asset.MediaAssetStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

/** Controlled bilingual identity inputs only; no arbitrary CSS or runtime code is accepted. */
@RestController
@RequestMapping("/api/v1/admin/site-settings")
@ConditionalOnExpression("!'${spring.autoconfigure.exclude:}'.contains('DataSourceAutoConfiguration')")
public class AdminSiteSettingsController {
    private static final String DEFAULT_PRESET = "EDITORIAL_NAVY";
    private static final String DEFAULT_DENSITY = "COMFORTABLE";
    private final JdbcTemplate jdbc;
    private final AuditEventRepository audit;
    private final ObjectMapper mapper;
    private final AuthenticatedAuditActor actor;
    private final MediaAssetRepository mediaAssets;

    public AdminSiteSettingsController(JdbcTemplate jdbc, AuditEventRepository audit, ObjectMapper mapper, AuthenticatedAuditActor actor, MediaAssetRepository mediaAssets) {
        this.jdbc = jdbc;
        this.audit = audit;
        this.mapper = mapper;
        this.actor = actor;
        this.mediaAssets = mediaAssets;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public SiteSettingsResponse get() {
        List<SiteSettingsResponse> rows = jdbc.query("select id,logo_media_id,og_media_id,theme_preset,layout_density,version from site_setting where deleted_at is null limit 1", (rs, row) -> response(rs.getObject("id", UUID.class), rs.getObject("logo_media_id", UUID.class), rs.getObject("og_media_id", UUID.class), rs.getString("theme_preset"), rs.getString("layout_density"), rs.getLong("version")));
        return rows.isEmpty() ? new SiteSettingsResponse(null, defaults("", ""), null, null, DEFAULT_PRESET, DEFAULT_DENSITY, null) : rows.getFirst();
    }

    @PutMapping
    @Transactional
    public ResponseEntity<SiteSettingsResponse> save(@Valid @RequestBody SiteSettingsRequest request) {
        Instant now = Instant.now();
        validateMediaReference(request.logoMediaId(), "Logo media");
        validateMediaReference(request.ogMediaId(), "Open Graph media");
        SiteSettingsResponse current = get();
        if (current.id() == null) {
            UUID id = UUID.randomUUID();
            jdbc.update("insert into site_setting (id,brand_name,brand_tagline,logo_media_id,og_media_id,theme_preset,layout_density,created_at,updated_at,version) values (?,?,?,?,?,?,?,?,?,0)", id, request.en().brandName().trim(), nullable(request.en().tagline()), request.logoMediaId(), request.ogMediaId(), request.themePreset(), request.layoutDensity(), timestamp(now), timestamp(now));
            saveTranslations(id, request, now);
            audit(id, now);
            return ResponseEntity.ok(response(id, request.logoMediaId(), request.ogMediaId(), request.themePreset(), request.layoutDensity(), 0L));
        }
        if (!current.version().equals(request.version())) throw new OptimisticLockingFailureException("Site settings changed; reload before saving");
        int changed = jdbc.update("update site_setting set brand_name=?,brand_tagline=?,logo_media_id=?,og_media_id=?,theme_preset=?,layout_density=?,updated_at=?,version=version+1 where id=? and deleted_at is null and version=?", request.en().brandName().trim(), nullable(request.en().tagline()), request.logoMediaId(), request.ogMediaId(), request.themePreset(), request.layoutDensity(), timestamp(now), current.id(), current.version());
        if (changed != 1) throw new OptimisticLockingFailureException("Site settings changed; reload before saving");
        saveTranslations(current.id(), request, now);
        audit(current.id(), now);
        return ResponseEntity.ok(response(current.id(), request.logoMediaId(), request.ogMediaId(), request.themePreset(), request.layoutDensity(), current.version() + 1));
    }

    private SiteSettingsResponse response(UUID id, UUID logo, UUID og, String preset, String density, Long version) { return new SiteSettingsResponse(id, translations(id), logo, og, preset, density, version); }
    private IdentityTranslations translations(UUID id) {
        if (id == null) return defaults("", "");
        List<IdentityTranslation> fa = jdbc.query("select brand_name,brand_tagline,footer_statement,footer_availability,footer_rights from site_setting_translation where site_setting_id=? and language_code='fa' and deleted_at is null", (rs, row) -> translation(rs), id);
        List<IdentityTranslation> en = jdbc.query("select brand_name,brand_tagline,footer_statement,footer_availability,footer_rights from site_setting_translation where site_setting_id=? and language_code='en' and deleted_at is null", (rs, row) -> translation(rs), id);
        return new IdentityTranslations(
                fa.isEmpty() ? emptyTranslation() : fa.getFirst(),
                en.isEmpty() ? emptyTranslation() : en.getFirst()
        );
    }
    private static IdentityTranslation translation(java.sql.ResultSet rs) throws java.sql.SQLException { return new IdentityTranslation(rs.getString("brand_name"), rs.getString("brand_tagline"), rs.getString("footer_statement"), rs.getString("footer_availability"), rs.getString("footer_rights")); }
    private static IdentityTranslation emptyTranslation() { return new IdentityTranslation("", null, null, null, null); }
    private static IdentityTranslations defaults(String fa, String en) { return new IdentityTranslations(new IdentityTranslation(fa, null, null, null, null), new IdentityTranslation(en, null, null, null, null)); }
    private void saveTranslations(UUID id, SiteSettingsRequest request, Instant now) { saveTranslation(id, "fa", request.fa(), now); saveTranslation(id, "en", request.en(), now); }
    private void saveTranslation(UUID id, String language, IdentityTranslationRequest value, Instant now) {
        int changed = jdbc.update("update site_setting_translation set brand_name=?,brand_tagline=?,footer_statement=?,footer_availability=?,footer_rights=?,updated_at=?,version=version+1 where site_setting_id=? and language_code=? and deleted_at is null", value.brandName().trim(), nullable(value.tagline()), nullable(value.footerStatement()), nullable(value.footerAvailability()), nullable(value.footerRights()), timestamp(now), id, language);
        if (changed == 0) jdbc.update("insert into site_setting_translation (id,site_setting_id,language_code,brand_name,brand_tagline,footer_statement,footer_availability,footer_rights,created_at,updated_at,version) values (?,?,?,?,?,?,?,?,?,?,0)", UUID.randomUUID(), id, language, value.brandName().trim(), nullable(value.tagline()), nullable(value.footerStatement()), nullable(value.footerAvailability()), nullable(value.footerRights()), timestamp(now), timestamp(now));
    }
    private static String nullable(String value) { return value == null || value.isBlank() ? null : value.trim(); }
    private static Timestamp timestamp(Instant value) { return Timestamp.from(value); }
    private void validateMediaReference(UUID mediaId, String field) {
        if (mediaId != null && mediaAssets.findByIdAndStatusAndDeletedAtIsNull(mediaId, MediaAssetStatus.ACTIVE).isEmpty()) {
            throw new IllegalArgumentException(field + " must reference an active media asset");
        }
    }
    private void audit(UUID id, Instant now) { audit.save(AuditEvent.record(UUID.randomUUID(), now, actor.required(), "ADMIN_SITE_SETTINGS_UPDATED", "SITE_SETTINGS", id, "SUCCESS", null, null, mapper.createObjectNode().put("changedFields", "identity"))); }

    public record SiteSettingsRequest(@Min(0) Long version, @NotNull @Valid IdentityTranslationRequest fa, @NotNull @Valid IdentityTranslationRequest en, UUID logoMediaId, UUID ogMediaId, @NotNull @Pattern(regexp = "EDITORIAL_NAVY") String themePreset, @NotNull @Pattern(regexp = "COMFORTABLE|STANDARD") String layoutDensity) { }
    public record IdentityTranslationRequest(@NotBlank @Size(max = 255) String brandName, @Size(max = 500) String tagline, @Size(max = 500) String footerStatement, @Size(max = 500) String footerAvailability, @Size(max = 500) String footerRights) { }
    public record SiteSettingsResponse(UUID id, IdentityTranslations translations, UUID logoMediaId, UUID ogMediaId, String themePreset, String layoutDensity, Long version) { }
    public record IdentityTranslations(IdentityTranslation fa, IdentityTranslation en) { }
    public record IdentityTranslation(String brandName, String tagline, String footerStatement, String footerAvailability, String footerRights) { }
}
