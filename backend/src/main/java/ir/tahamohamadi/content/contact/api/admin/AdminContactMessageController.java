package ir.tahamohamadi.content.contact.api.admin;
import ir.tahamohamadi.common.api.PageResponse;
import ir.tahamohamadi.content.contact.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;

@RestController @RequestMapping("/api/v1/admin/contact-messages") @ConditionalOnExpression("!'${spring.autoconfigure.exclude:}'.contains('DataSourceAutoConfiguration')")
public class AdminContactMessageController {
    private final ContactMessageRepository messages; public AdminContactMessageController(ContactMessageRepository messages){this.messages=messages;}
    @GetMapping PageResponse<Map<String,Object>> list(@RequestParam(defaultValue="NEW") ContactMessageStatus status,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size){if(page<0||size<1||size>100)throw new IllegalArgumentException("Invalid pagination");return PageResponse.from(messages.findByStatusOrderBySubmittedAtDescIdDesc(status,PageRequest.of(page,size)).map(m->Map.<String,Object>of("id",m.getId(),"senderName",m.getSenderName(),"senderEmail",m.getSenderEmail(),"status",m.getStatus(),"submittedAt",m.getSubmittedAt())));}
    @GetMapping("/{id}") Map<String,Object> detail(@PathVariable UUID id){ContactMessage m=messages.findById(id).orElseThrow(()->new NoSuchElementException("Contact message not found"));return Map.of("id",m.getId(),"senderName",m.getSenderName(),"senderEmail",m.getSenderEmail(),"message",m.messageForAdministration(),"language",m.getSourceLanguage(),"status",m.getStatus(),"submittedAt",m.getSubmittedAt());}
    @PostMapping("/{id}/read") @Transactional Map<String,Object> read(@PathVariable UUID id){ContactMessage m=messages.findById(id).orElseThrow(()->new NoSuchElementException("Contact message not found"));m.markRead(Instant.now());return Map.of("id",m.getId(),"status",m.getStatus());}
    @PostMapping("/{id}/archive") @Transactional ResponseEntity<Void> archive(@PathVariable UUID id){ContactMessage m=messages.findById(id).orElseThrow(()->new NoSuchElementException("Contact message not found"));m.archive(Instant.now());return ResponseEntity.noContent().build();}
}
