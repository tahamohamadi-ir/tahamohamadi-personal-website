package ir.tahamohamadi.portfolio.project.api.admin;

import ir.tahamohamadi.common.api.PageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@Validated
@RequestMapping("/api/v1/admin/portfolio/projects")
public class AdminProjectController {
    private final AdminProjectService projects;

    public AdminProjectController(AdminProjectService projects) { this.projects = projects; }

    @GetMapping
    public PageResponse<AdminProjectSummary> list(@RequestParam(defaultValue = "0") @Min(0) int page, @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size, @RequestParam(defaultValue = "updatedAt,desc") String sort) {
        return PageResponse.from(projects.list(page, size, sort));
    }

    @GetMapping("/{id}") public AdminProjectResponse get(@PathVariable UUID id) { return projects.get(id); }
    @PostMapping public ResponseEntity<AdminProjectResponse> create(@Valid @RequestBody AdminProjectCreateRequest request) { return ResponseEntity.status(HttpStatus.CREATED).body(projects.create(request)); }
    @PutMapping("/{id}") public AdminProjectResponse update(@PathVariable UUID id, @Valid @RequestBody AdminProjectUpdateRequest request) { return projects.update(id, request); }
    @PostMapping("/{id}/publish") public AdminProjectResponse publish(@PathVariable UUID id, @RequestParam long version) { return projects.publish(id, version); }
    @PostMapping("/{id}/archive") public AdminProjectResponse archive(@PathVariable UUID id, @RequestParam long version) { return projects.archive(id, version); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@PathVariable UUID id, @RequestParam long version) { projects.delete(id, version); }
}
