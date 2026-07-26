package ir.tahamohamadi.content.page.api.admin;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/pages/{pageId}/preview-token")
@ConditionalOnExpression("!'${spring.autoconfigure.exclude:}'.contains('DataSourceAutoConfiguration')")
public class AdminPagePreviewTokenController {
    private final PagePreviewTokenService tokens;
    public AdminPagePreviewTokenController(PagePreviewTokenService tokens) { this.tokens = tokens; }

    @PostMapping
    public ResponseEntity<PreviewTokenResponse> issue(@PathVariable UUID pageId) {
        var issued = tokens.issue(pageId);
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(new PreviewTokenResponse(issued.token(), issued.expiresAt()));
    }
    public record PreviewTokenResponse(String token, java.time.Instant expiresAt) { }
}
