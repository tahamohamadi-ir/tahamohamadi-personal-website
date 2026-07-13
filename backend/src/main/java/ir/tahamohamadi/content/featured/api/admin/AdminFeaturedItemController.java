package ir.tahamohamadi.content.featured.api.admin;

import ir.tahamohamadi.common.api.PageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@Validated
@RequestMapping("/api/v1/admin/featured-items")
public class AdminFeaturedItemController {
    private final AdminFeaturedItemService service;
    public AdminFeaturedItemController(AdminFeaturedItemService service) { this.service = service; }
    @GetMapping public PageResponse<AdminFeaturedItemResponse> list(@RequestParam(defaultValue = "0") @Min(0) int page, @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size, @RequestParam(defaultValue = "updatedAt,desc") String sort) { return PageResponse.from(service.list(page, size, sort)); }
    @GetMapping("/{id}") public AdminFeaturedItemResponse get(@PathVariable UUID id) { return service.get(id); }
    @PostMapping public ResponseEntity<AdminFeaturedItemResponse> create(@Valid @RequestBody AdminFeaturedItemRequest request) { return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request)); }
    @PutMapping("/{id}") public AdminFeaturedItemResponse update(@PathVariable UUID id, @Valid @RequestBody AdminFeaturedItemRequest request) { return service.update(id, request); }
    @PostMapping("/{id}/activate") public AdminFeaturedItemResponse activate(@PathVariable UUID id, @RequestParam @Min(0) long version) { return service.activate(id, version); }
    @PostMapping("/{id}/deactivate") public AdminFeaturedItemResponse deactivate(@PathVariable UUID id, @RequestParam @Min(0) long version) { return service.deactivate(id, version); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@PathVariable UUID id, @RequestParam @Min(0) long version) { service.delete(id, version); }
}
