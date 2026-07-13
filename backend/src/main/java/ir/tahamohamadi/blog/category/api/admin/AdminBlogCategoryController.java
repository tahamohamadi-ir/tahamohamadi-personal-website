package ir.tahamohamadi.blog.category.api.admin;

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
@RequestMapping("/api/v1/admin/blog/categories")
public class AdminBlogCategoryController {
    private final AdminBlogCategoryService categories;
    public AdminBlogCategoryController(AdminBlogCategoryService categories) { this.categories=categories; }
    @GetMapping public PageResponse<AdminBlogCategoryResponse> list(@RequestParam(defaultValue="0") @Min(0) int page,@RequestParam(defaultValue="20") @Min(1) @Max(100) int size) { return PageResponse.from(categories.list(PageRequest.of(page,size))); }
    @GetMapping("/{id}") public AdminBlogCategoryResponse get(@PathVariable UUID id) { return categories.get(id); }
    @PostMapping public ResponseEntity<AdminBlogCategoryResponse> create(@Valid @RequestBody AdminBlogCategoryRequest request) { return ResponseEntity.status(HttpStatus.CREATED).body(categories.create(request)); }
    @PutMapping("/{id}") public AdminBlogCategoryResponse update(@PathVariable UUID id,@Valid @RequestBody AdminBlogCategoryRequest request) { return categories.update(id,request); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> deactivate(@PathVariable UUID id,@RequestParam long version) { categories.deactivate(id,version); return ResponseEntity.noContent().build(); }
}
