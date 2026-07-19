package ir.tahamohamadi.skill.api.admin;

import ir.tahamohamadi.common.api.PageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/skills/categories")
public class AdminSkillCategoryController {
    private final AdminSkillCategoryService categories;
    public AdminSkillCategoryController(AdminSkillCategoryService categories) { this.categories = categories; }

    @GetMapping
    public PageResponse<AdminSkillCategoryResponse> list(@RequestParam(defaultValue = "0") @Min(0) int page, @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size, @RequestParam(defaultValue = "sortOrder,asc") String sort) { return PageResponse.from(categories.list(page, size, sort)); }
    @GetMapping("/{id}") public AdminSkillCategoryResponse get(@PathVariable UUID id) { return categories.get(id); }
    @PostMapping public ResponseEntity<AdminSkillCategoryResponse> create(@Valid @RequestBody AdminSkillCategoryRequest request) { return ResponseEntity.status(HttpStatus.CREATED).body(categories.create(request)); }
    @PutMapping("/{id}") public AdminSkillCategoryResponse update(@PathVariable UUID id, @Valid @RequestBody AdminSkillCategoryRequest request) { return categories.update(id, request); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> deactivate(@PathVariable UUID id, @RequestParam long version) { categories.deactivate(id, version); return ResponseEntity.noContent().build(); }
}
