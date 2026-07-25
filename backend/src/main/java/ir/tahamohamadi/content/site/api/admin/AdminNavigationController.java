package ir.tahamohamadi.content.site.api.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.tahamohamadi.audit.event.AuditEvent;
import ir.tahamohamadi.audit.event.AuditEventRepository;
import ir.tahamohamadi.common.audit.AuthenticatedAuditActor;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Ordered, bilingual, structured primary navigation. */
@RestController
@RequestMapping("/api/v1/admin/navigation")
@ConditionalOnExpression("!'${spring.autoconfigure.exclude:}'.contains('DataSourceAutoConfiguration')")
public class AdminNavigationController {
    private final JdbcTemplate jdbc;
    private final AuditEventRepository audit;
    private final ObjectMapper mapper;
    private final AuthenticatedAuditActor actor;

    public AdminNavigationController(JdbcTemplate jdbc, AuditEventRepository audit, ObjectMapper mapper, AuthenticatedAuditActor actor) { this.jdbc = jdbc; this.audit = audit; this.mapper = mapper; this.actor = actor; }

    @GetMapping
    @Transactional(readOnly = true)
    public NavigationResponse get() { return new NavigationResponse(items()); }

    @PutMapping
    @Transactional
    public NavigationResponse replace(@Valid @RequestBody NavigationRequest request) {
        List<NavigationItemResponse> existing = items();
        Map<UUID, NavigationItemResponse> current = new HashMap<>();
        existing.forEach(item -> current.put(item.id(), item));
        for (NavigationItemRequest item : request.items()) {
            if (item.id() != null) {
                NavigationItemResponse stored = current.get(item.id());
                if (stored == null || item.version() == null || stored.version() != item.version()) {
                    throw new OptimisticLockingFailureException("Navigation changed; reload before saving");
                }
            }
        }
        Instant now = Instant.now();
        Map<UUID, Boolean> retained = new HashMap<>();
        for (int order = 0; order < request.items().size(); order++) {
            NavigationItemRequest item = request.items().get(order);
            validateTarget(item);
            UUID id = item.id() == null ? UUID.randomUUID() : item.id();
            retained.put(id, true);
            if (item.id() == null) {
                jdbc.update("insert into site_navigation_item (id,navigation_key,target_path,external_target,sort_order,is_active,created_at,updated_at,version) values (?,?,?,?,?,?,?,?,0)", id, item.key().trim(), item.targetPath().trim(), item.externalTarget(), order, item.active(), timestamp(now), timestamp(now));
                insertTranslation(id, "fa", item.fa(), now);
                insertTranslation(id, "en", item.en(), now);
            } else {
                int changed = jdbc.update("update site_navigation_item set navigation_key=?,target_path=?,external_target=?,sort_order=?,is_active=?,updated_at=?,version=version+1 where id=? and deleted_at is null and version=?", item.key().trim(), item.targetPath().trim(), item.externalTarget(), order, item.active(), timestamp(now), id, item.version());
                if (changed != 1) throw new OptimisticLockingFailureException("Navigation changed; reload before saving");
                updateTranslation(id, "fa", item.fa(), now);
                updateTranslation(id, "en", item.en(), now);
            }
        }
        for (NavigationItemResponse item : existing) {
            if (!retained.containsKey(item.id())) {
                int changed = jdbc.update("update site_navigation_item set deleted_at=?,updated_at=?,version=version+1 where id=? and deleted_at is null and version=?", timestamp(now), timestamp(now), item.id(), item.version());
                if (changed != 1) throw new OptimisticLockingFailureException("Navigation changed; reload before saving");
            }
        }
        List<NavigationItemResponse> saved = items();
        audit.save(AuditEvent.record(UUID.randomUUID(), now, actor.required(), "ADMIN_NAVIGATION_UPDATED", "SITE_NAVIGATION", null, "SUCCESS", null, null, mapper.createObjectNode().put("itemCount", saved.size())));
        return new NavigationResponse(saved);
    }

    private void validateTarget(NavigationItemRequest item) {
        String path = item.targetPath().trim();
        boolean valid = item.externalTarget() ? path.startsWith("https://") : path.matches("^/(fa|en)(/.*)?$") || path.matches("^/\\{lang\\}(/.*)?$");
        if (!valid) throw new IllegalArgumentException("Navigation target must be HTTPS or a localized internal route template");
    }

    private void insertTranslation(UUID id, String language, TranslationRequest value, Instant now) {
        jdbc.update("insert into site_navigation_item_translation (id,site_navigation_item_id,language_code,label,created_at,updated_at,version) values (?,?,?,?,?,?,0)", UUID.randomUUID(), id, language, value.label().trim(), timestamp(now), timestamp(now));
    }

    private void updateTranslation(UUID id, String language, TranslationRequest value, Instant now) {
        jdbc.update("update site_navigation_item_translation set label=?,updated_at=?,version=version+1 where site_navigation_item_id=? and language_code=? and deleted_at is null", value.label().trim(), timestamp(now), id, language);
    }

    private List<NavigationItemResponse> items() {
        return jdbc.query("select n.id,n.navigation_key,n.target_path,n.external_target,n.sort_order,n.is_active,n.version,fa.label fa_label,en.label en_label from site_navigation_item n join site_navigation_item_translation fa on fa.site_navigation_item_id=n.id and fa.language_code='fa' and fa.deleted_at is null join site_navigation_item_translation en on en.site_navigation_item_id=n.id and en.language_code='en' and en.deleted_at is null where n.deleted_at is null order by n.sort_order,n.id", (rs, row) -> new NavigationItemResponse(rs.getObject("id", UUID.class), rs.getString("navigation_key"), rs.getString("target_path"), rs.getBoolean("external_target"), rs.getInt("sort_order"), rs.getBoolean("is_active"), rs.getLong("version"), new TranslationResponse(rs.getString("fa_label")), new TranslationResponse(rs.getString("en_label"))));
    }

    private static Timestamp timestamp(Instant value) { return Timestamp.from(value); }

    public record NavigationRequest(@NotNull @Size(max = 12) List<@Valid NavigationItemRequest> items) { }
    public record NavigationItemRequest(UUID id, @Min(0) Long version, @NotBlank @Size(max = 100) String key, @NotBlank @Size(max = 2048) String targetPath, boolean externalTarget, boolean active, @NotNull @Valid TranslationRequest fa, @NotNull @Valid TranslationRequest en) { }
    public record TranslationRequest(@NotBlank @Size(max = 255) String label) { }
    public record NavigationResponse(List<NavigationItemResponse> items) { }
    public record NavigationItemResponse(UUID id, String key, String targetPath, boolean externalTarget, int sortOrder, boolean active, long version, TranslationResponse fa, TranslationResponse en) { }
    public record TranslationResponse(String label) { }
}
