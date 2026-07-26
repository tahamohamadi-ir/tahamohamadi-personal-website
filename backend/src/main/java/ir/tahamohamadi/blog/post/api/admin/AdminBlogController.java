package ir.tahamohamadi.blog.post.api.admin;

import ir.tahamohamadi.common.api.PageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.List;
import java.time.Instant;

@RestController
@RequestMapping("/api/v1/admin/blog/posts")
@ConditionalOnExpression("!'${spring.autoconfigure.exclude:}'.contains('DataSourceAutoConfiguration')")
public class AdminBlogController {
    private final AdminBlogService blog;
    public AdminBlogController(AdminBlogService blog) { this.blog=blog; }
    @GetMapping public PageResponse<AdminBlogSummary> list(@RequestParam(defaultValue="0") @Min(0) int page,@RequestParam(defaultValue="20") @Min(1) @Max(100) int size) { return PageResponse.from(blog.list(PageRequest.of(page,size))); }
    @GetMapping("/{id}") public AdminBlogResponse get(@PathVariable UUID id) { return blog.get(id); }
    @PostMapping public ResponseEntity<AdminBlogResponse> create(@Valid @RequestBody AdminBlogCreateRequest request) { return ResponseEntity.status(HttpStatus.CREATED).body(blog.create(request)); }
    @PutMapping("/{id}") public AdminBlogResponse update(@PathVariable UUID id,@Valid @RequestBody AdminBlogUpdateRequest request) { return blog.update(id,request); }
    @PostMapping("/{id}/publish") public AdminBlogResponse publish(@PathVariable UUID id,@RequestParam long version) { return blog.publish(id,version); }
    @PostMapping("/{id}/schedule") public AdminBlogResponse schedule(@PathVariable UUID id,@RequestParam long version,@RequestParam Instant scheduledFor) { return blog.schedule(id,version,scheduledFor); }
    @PostMapping("/{id}/cancel-schedule") public AdminBlogResponse cancelSchedule(@PathVariable UUID id,@RequestParam long version) { return blog.cancelSchedule(id,version); }
    @PostMapping("/{id}/archive") public AdminBlogResponse archive(@PathVariable UUID id,@RequestParam long version) { return blog.archive(id,version); }
    @GetMapping("/{id}/revisions") public List<AdminBlogRevisionSummary> revisions(@PathVariable UUID id) { return blog.revisions(id); }
    @GetMapping("/{id}/revisions/{revisionId}") public AdminBlogRevisionResponse revision(@PathVariable UUID id, @PathVariable UUID revisionId) { return blog.revisionDetail(id, revisionId); }
    @PostMapping("/{id}/revisions/{revisionId}/restore-as-draft") public AdminBlogResponse restoreAsDraft(@PathVariable UUID id, @PathVariable UUID revisionId, @RequestParam long version) { return blog.restoreAsDraft(id, revisionId, version); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable UUID id,@RequestParam long version) { blog.delete(id,version); return ResponseEntity.noContent().build(); }
}
