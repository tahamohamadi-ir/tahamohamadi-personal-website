package ir.tahamohamadi.media.api.admin;

import ir.tahamohamadi.common.api.PageResponse;
import ir.tahamohamadi.media.service.MediaAssetService;
import ir.tahamohamadi.media.service.MediaOrphanReportService;
import jakarta.validation.Valid;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.io.IOException;
import ir.tahamohamadi.media.asset.MediaAssetStatus;
import ir.tahamohamadi.media.storage.MediaStorage;

@RestController
@RequestMapping("/api/v1/admin/media")
@ConditionalOnExpression("!'${spring.autoconfigure.exclude:}'.contains('DataSourceAutoConfiguration')")
public class AdminMediaController {
    private final MediaAssetService media;
    private final MediaOrphanReportService orphans;
    private final MediaStorage storage;

    public AdminMediaController(MediaAssetService media, MediaOrphanReportService orphans, MediaStorage storage) {
        this.media = media;
        this.orphans = orphans;
        this.storage = storage;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<MediaAssetResponse> upload(
            @RequestPart MultipartFile file,
            @RequestParam(required = false) String faAlt,
            @RequestParam(required = false) String faCaption,
            @RequestParam(required = false) String enAlt,
            @RequestParam(required = false) String enCaption
    ) {
        MediaAssetResponse response = media.upload(file, faAlt, faCaption, enAlt, enCaption);
        return ResponseEntity.created(URI.create("/api/v1/admin/media/" + response.id())).body(response);
    }

    @GetMapping
    PageResponse<MediaAssetSummary> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) MediaAssetStatus status
    ) {
        return PageResponse.from(media.list(
                PageRequest.of(
                        validPage(page),
                        validSize(size),
                        Sort.by(Sort.Order.desc("updatedAt"), Sort.Order.desc("id"))
                ),
                validQuery(query),
                mimePrefix(type),
                status
        ));
    }

    @GetMapping("/{id}")
    MediaAssetResponse get(@PathVariable UUID id) {
        return media.get(id);
    }

    @GetMapping("/{id}/content")
    ResponseEntity<InputStreamResource> content(@PathVariable UUID id) throws IOException {
        var asset = media.adminReadable(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(asset.getMimeType()))
                .contentLength(asset.getSizeBytes())
                .cacheControl(CacheControl.noStore().cachePrivate().noTransform())
                .header("X-Content-Type-Options", "nosniff")
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .body(new InputStreamResource(storage.open(asset.getStorageKey())));
    }

    @GetMapping("/{id}/usage")
    List<MediaUsageResponse> usage(@PathVariable UUID id) {
        media.adminReadable(id);
        return orphans.usages(id);
    }

    @PutMapping("/{id}/metadata")
    MediaAssetResponse metadata(
            @PathVariable UUID id,
            @Valid @RequestBody MediaMetadataRequest request
    ) {
        return media.update(id, request);
    }

    @PostMapping("/{id}/replace")
    MediaAssetResponse replace(
            @PathVariable UUID id,
            @Valid @RequestBody MediaReplaceRequest request
    ) {
        return media.replace(id, request);
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> archive(
            @PathVariable UUID id,
            @RequestParam long version
    ) {
        media.archive(id, version);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/orphans")
    List<MediaOrphanResponse> orphans() {
        return orphans.findOrphans();
    }

    private static int validPage(int page) {
        if (page < 0) {
            throw new IllegalArgumentException("page must be nonnegative");
        }
        return page;
    }

    private static int validSize(int size) {
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("size must be between 1 and 100");
        }
        return size;
    }

    private static String validQuery(String query) {
        if (query == null || query.isBlank()) {
            return null;
        }
        if (query.length() > 100) {
            throw new IllegalArgumentException("query must not exceed 100 characters");
        }
        return query.trim();
    }

    private static String mimePrefix(String type) {
        if (type == null || type.isBlank()) {
            return null;
        }
        return switch (type) {
            case "image" -> "image/";
            case "document" -> "application/pdf";
            default -> throw new IllegalArgumentException("type must be image or document");
        };
    }
}
