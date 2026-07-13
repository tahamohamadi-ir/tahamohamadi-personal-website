package ir.tahamohamadi.content.contact.api.publicsite;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
@RestController @RequestMapping("/api/v1/public/contact") @ConditionalOnExpression("!'${spring.autoconfigure.exclude:}'.contains('DataSourceAutoConfiguration')")
public class PublicContactController {
    private final PublicContactService contacts; public PublicContactController(PublicContactService contacts){this.contacts=contacts;}
    @PostMapping ResponseEntity<ContactSubmissionResponse> submit(@Valid @RequestBody ContactSubmissionRequest request,HttpServletRequest http){return ResponseEntity.status(HttpStatus.CREATED).body(contacts.submit(request,http.getRemoteAddr()));}
}
