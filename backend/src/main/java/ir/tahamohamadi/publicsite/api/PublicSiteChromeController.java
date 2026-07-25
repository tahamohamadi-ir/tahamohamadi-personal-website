package ir.tahamohamadi.publicsite.api;

import ir.tahamohamadi.common.domain.LanguageCode;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.List;

/** Public, published-only shell data. It intentionally contains no draft or admin state. */
@RestController
@RequestMapping("/api/v1/public/{lang}/site")
@ConditionalOnExpression("!'${spring.autoconfigure.exclude:}'.contains('DataSourceAutoConfiguration')")
public class PublicSiteChromeController {
    private final JdbcTemplate jdbc;

    public PublicSiteChromeController(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @GetMapping
    public ResponseEntity<SiteChromeResponse> get(@PathVariable LanguageCode lang) {
        SiteIdentity identity = jdbc.query(
                "select t.brand_name,t.brand_tagline,t.footer_statement,t.footer_availability,t.footer_rights,s.logo_media_id,s.og_media_id,s.theme_preset,s.layout_density from site_setting s join site_setting_translation t on t.site_setting_id=s.id and t.language_code=? and t.deleted_at is null where s.deleted_at is null limit 1",
                rs -> rs.next() ? new SiteIdentity(rs.getString("brand_name"), rs.getString("brand_tagline"), rs.getString("footer_statement"), rs.getString("footer_availability"), rs.getString("footer_rights"), rs.getObject("logo_media_id", java.util.UUID.class), rs.getObject("og_media_id", java.util.UUID.class), rs.getString("theme_preset"), rs.getString("layout_density")) : null, lang.name());
        List<SiteNavigationItem> navigation = jdbc.query(
                "select n.navigation_key,n.target_path,n.external_target,t.label from site_navigation_item n join site_navigation_item_translation t on t.site_navigation_item_id=n.id and t.language_code=? and t.deleted_at is null where n.deleted_at is null and n.is_active=true order by n.sort_order,n.id",
                (rs, row) -> new SiteNavigationItem(rs.getString("navigation_key"), rs.getString("target_path"), rs.getBoolean("external_target"), rs.getString("label")), lang.name().toLowerCase());
        return ResponseEntity.ok().cacheControl(CacheControl.maxAge(Duration.ofMinutes(5)).cachePublic()).body(new SiteChromeResponse(identity, navigation));
    }

    public record SiteChromeResponse(SiteIdentity identity, List<SiteNavigationItem> navigation) { }
    public record SiteIdentity(String brandName, String tagline, String footerStatement, String footerAvailability, String footerRights, java.util.UUID logoMediaId, java.util.UUID ogMediaId, String themePreset, String layoutDensity) { }
    public record SiteNavigationItem(String key, String targetPath, boolean externalTarget, String label) { }
}
