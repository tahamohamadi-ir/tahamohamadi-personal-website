package ir.tahamohamadi.content.featured.api.publicsite;

import ir.tahamohamadi.common.domain.LanguageCode;
import jakarta.validation.constraints.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/api/v1/public/{locale}/featured")
public class PublicFeaturedController {
    private final PublicFeaturedService service;
    public PublicFeaturedController(PublicFeaturedService service) { this.service = service; }
    @GetMapping public PublicFeaturedResponse list(@PathVariable LanguageCode locale, @RequestParam(defaultValue="home") @NotBlank @Size(max=100) String slot, @RequestParam(defaultValue="3") @Min(1) @Max(50) int size) { return service.list(locale,slot,size); }
}
