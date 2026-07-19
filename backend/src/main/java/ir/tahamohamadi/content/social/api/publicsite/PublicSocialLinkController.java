package ir.tahamohamadi.content.social.api.publicsite;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/public/social-links")
public class PublicSocialLinkController {
    private final PublicSocialLinkService service;
    public PublicSocialLinkController(PublicSocialLinkService service) { this.service = service; }
    @GetMapping public PublicSocialLinksResponse list() { return service.list(); }
}
