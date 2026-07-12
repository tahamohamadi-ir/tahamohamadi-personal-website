package ir.tahamohamadi.media.api.admin;

import ir.tahamohamadi.common.api.PageResponse;
import ir.tahamohamadi.media.service.MediaAssetService;
import ir.tahamohamadi.media.service.MediaOrphanReportService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController @RequestMapping("/api/v1/admin/media") @ConditionalOnExpression("!'${spring.autoconfigure.exclude:}'.contains('DataSourceAutoConfiguration')")
public class AdminMediaController {
    private final MediaAssetService media; private final MediaOrphanReportService orphans;
    public AdminMediaController(MediaAssetService media, MediaOrphanReportService orphans) { this.media=media; this.orphans=orphans; }
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<MediaAssetResponse> upload(@RequestPart MultipartFile file,@RequestParam String faAlt,@RequestParam(required=false) String faCaption,@RequestParam String enAlt,@RequestParam(required=false) String enCaption) { MediaAssetResponse response=media.upload(file,faAlt,faCaption,enAlt,enCaption); return ResponseEntity.created(URI.create("/api/v1/admin/media/"+response.id())).body(response); }
    @GetMapping PageResponse<MediaAssetSummary> list(@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size) { return PageResponse.from(media.list(PageRequest.of(validPage(page),validSize(size)))); }
    @GetMapping("/{id}") MediaAssetResponse get(@PathVariable UUID id) { return media.get(id); }
    @PutMapping("/{id}/metadata") MediaAssetResponse metadata(@PathVariable UUID id,@Valid @RequestBody MediaMetadataRequest request) { return media.update(id,request); }
    @DeleteMapping("/{id}") ResponseEntity<Void> archive(@PathVariable UUID id) { media.archive(id); return ResponseEntity.noContent().build(); }
    @GetMapping("/orphans") List<MediaOrphanResponse> orphans() { return orphans.findOrphans(); }
    private static int validPage(int page) { if(page<0) throw new IllegalArgumentException("page must be nonnegative"); return page; }
    private static int validSize(int size) { if(size<1||size>100) throw new IllegalArgumentException("size must be between 1 and 100"); return size; }
}
