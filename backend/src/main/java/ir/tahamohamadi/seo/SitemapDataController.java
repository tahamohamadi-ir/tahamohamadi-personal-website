package ir.tahamohamadi.seo;

import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.Duration;

@RestController
public class SitemapDataController {
    private final SitemapDataService service;
    public SitemapDataController(SitemapDataService service) { this.service = service; }
    @GetMapping("/api/v1/public/sitemap-data")
    ResponseEntity<SitemapDataResponse> sitemapData() { return ResponseEntity.ok().cacheControl(CacheControl.maxAge(Duration.ofMinutes(5)).cachePublic()).body(service.listPublishedRoutes()); }
}
