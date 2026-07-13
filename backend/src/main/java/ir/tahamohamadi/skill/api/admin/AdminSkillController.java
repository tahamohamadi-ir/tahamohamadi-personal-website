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
@RequestMapping("/api/v1/admin/skills")
public class AdminSkillController {
    private final AdminSkillService skills;
    public AdminSkillController(AdminSkillService skills) { this.skills = skills; }

    @GetMapping
    public PageResponse<AdminSkillResponse> list(@RequestParam(defaultValue = "0") @Min(0) int page, @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size, @RequestParam(defaultValue = "sortOrder,asc") String sort) { return PageResponse.from(skills.list(page, size, sort)); }
    @GetMapping("/{id}") public AdminSkillResponse get(@PathVariable UUID id) { return skills.get(id); }
    @PostMapping public ResponseEntity<AdminSkillResponse> create(@Valid @RequestBody AdminSkillRequest request) { return ResponseEntity.status(HttpStatus.CREATED).body(skills.create(request)); }
    @PutMapping("/{id}") public AdminSkillResponse update(@PathVariable UUID id, @Valid @RequestBody AdminSkillRequest request) { return skills.update(id, request); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> deactivate(@PathVariable UUID id, @RequestParam long version) { skills.deactivate(id, version); return ResponseEntity.noContent().build(); }
}
