package ir.tahamohamadi.media.api.publicsite;

import ir.tahamohamadi.media.service.MediaAssetService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.util.UUID;
import ir.tahamohamadi.media.storage.MediaStorage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;

@RestController @RequestMapping("/api/v1/public/media") @ConditionalOnExpression("!'${spring.autoconfigure.exclude:}'.contains('DataSourceAutoConfiguration')")
public class PublicMediaController {
    private final MediaAssetService media; private final MediaStorage storage;
    public PublicMediaController(MediaAssetService media, MediaStorage storage) { this.media=media; this.storage=storage; }
    @GetMapping("/{id}") ResponseEntity<InputStreamResource> get(@PathVariable UUID id) throws IOException {
        var asset=media.activePublic(id);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(asset.getMimeType())).contentLength(asset.getSizeBytes())
                .eTag('"'+asset.getChecksumSha256()+'"').cacheControl(CacheControl.maxAge(java.time.Duration.ofHours(1)).cachePublic().noTransform())
                .header("X-Content-Type-Options","nosniff").header(HttpHeaders.CONTENT_DISPOSITION,"attachment")
                .body(new InputStreamResource(storage.open(asset.getStorageKey())));
    }
}
