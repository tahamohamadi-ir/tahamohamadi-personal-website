package ir.tahamohamadi.blog.tag.api.admin;

import ir.tahamohamadi.common.api.PageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/blog/tags")
public class AdminTagController {
    private final AdminTagService tags;
    public AdminTagController(AdminTagService tags) { this.tags=tags; }
    @GetMapping public PageResponse<AdminTagResponse> list(@RequestParam(defaultValue="0") @Min(0) int page,@RequestParam(defaultValue="20") @Min(1) @Max(100) int size) { return PageResponse.from(tags.list(PageRequest.of(page,size))); }
    @GetMapping("/{id}") public AdminTagResponse get(@PathVariable UUID id) { return tags.get(id); }
    @PostMapping public ResponseEntity<AdminTagResponse> create(@Valid @RequestBody AdminTagRequest request) { return ResponseEntity.status(HttpStatus.CREATED).body(tags.create(request)); }
    @PutMapping("/{id}") public AdminTagResponse update(@PathVariable UUID id,@Valid @RequestBody AdminTagRequest request) { return tags.update(id,request); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> deactivate(@PathVariable UUID id,@RequestParam long version) { tags.deactivate(id,version); return ResponseEntity.noContent().build(); }
}
