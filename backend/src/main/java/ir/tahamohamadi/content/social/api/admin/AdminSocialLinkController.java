package ir.tahamohamadi.content.social.api.admin;

import ir.tahamohamadi.common.api.PageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@Validated
@RequestMapping("/api/v1/admin/social-links")
public class AdminSocialLinkController {
    private final AdminSocialLinkService service;
    public AdminSocialLinkController(AdminSocialLinkService service) { this.service = service; }
    @GetMapping public PageResponse<AdminSocialLinkResponse> list(@RequestParam(defaultValue="0") @Min(0) int page,@RequestParam(defaultValue="20") @Min(1) @Max(100) int size,@RequestParam(defaultValue="updatedAt,desc") String sort) { return PageResponse.from(service.list(page,size,sort)); }
    @GetMapping("/{id}") public AdminSocialLinkResponse get(@PathVariable UUID id) { return service.get(id); }
    @PostMapping public ResponseEntity<AdminSocialLinkResponse> create(@Valid @RequestBody AdminSocialLinkRequest request) { return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request)); }
    @PutMapping("/{id}") public AdminSocialLinkResponse update(@PathVariable UUID id,@Valid @RequestBody AdminSocialLinkRequest request) { return service.update(id,request); }
    @PostMapping("/{id}/activate") public AdminSocialLinkResponse activate(@PathVariable UUID id,@RequestParam @Min(0) long version) { return service.activate(id,version); }
    @PostMapping("/{id}/deactivate") public AdminSocialLinkResponse deactivate(@PathVariable UUID id,@RequestParam @Min(0) long version) { return service.deactivate(id,version); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@PathVariable UUID id,@RequestParam @Min(0) long version) { service.delete(id,version); }
}
