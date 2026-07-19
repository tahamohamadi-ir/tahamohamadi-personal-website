package ir.tahamohamadi.content.page.api.admin;

import ir.tahamohamadi.common.api.PageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/pages")
@ConditionalOnExpression("!'${spring.autoconfigure.exclude:}'.contains('DataSourceAutoConfiguration')")
public class AdminPageController {
    private final AdminPageService pages; public AdminPageController(AdminPageService pages) { this.pages=pages; }
    @GetMapping public PageResponse<AdminPageResponse> list(@RequestParam(defaultValue="0") @Min(0) int page,@RequestParam(defaultValue="20") @Min(1) @Max(100) int size) { return PageResponse.from(pages.list(PageRequest.of(page,size))); }
    @GetMapping("/{id}") public AdminPageResponse get(@PathVariable UUID id) { return pages.get(id); }
    @PostMapping public ResponseEntity<AdminPageResponse> create(@Valid @RequestBody AdminPageRequest request) { AdminPageResponse result=pages.create(request); return ResponseEntity.created(URI.create("/api/v1/admin/pages/"+result.id())).body(result); }
    @PutMapping("/{id}") public AdminPageResponse update(@PathVariable UUID id,@Valid @RequestBody AdminPageRequest request) { return pages.update(id,request); }
    @PostMapping("/{id}/publish") public AdminPageResponse publish(@PathVariable UUID id,@RequestParam long version) { return pages.publish(id,version); }
    @PostMapping("/{id}/archive") public AdminPageResponse archive(@PathVariable UUID id,@RequestParam long version) { return pages.archive(id,version); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable UUID id,@RequestParam long version) { pages.delete(id,version); return ResponseEntity.noContent().build(); }
}
