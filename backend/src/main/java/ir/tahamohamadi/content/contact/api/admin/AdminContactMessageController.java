package ir.tahamohamadi.content.contact.api.admin;

import ir.tahamohamadi.common.api.PageResponse;
import ir.tahamohamadi.content.contact.ContactMessageStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/contact-messages")
@ConditionalOnExpression("!'${spring.autoconfigure.exclude:}'.contains('DataSourceAutoConfiguration')")
public class AdminContactMessageController {
    private final AdminContactMessageService messages;

    public AdminContactMessageController(AdminContactMessageService messages) {
        this.messages = messages;
    }

    @GetMapping
    PageResponse<AdminContactMessageSummary> list(
            @RequestParam(defaultValue = "NEW") ContactMessageStatus status,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        return PageResponse.from(messages.list(status, PageRequest.of(page, size)));
    }

    @GetMapping("/{id}")
    AdminContactMessageDetail detail(@PathVariable UUID id) {
        return messages.get(id);
    }

    @PostMapping("/{id}/read")
    AdminContactMessageStatusResponse read(@PathVariable UUID id) {
        return messages.markRead(id);
    }

    @PostMapping("/{id}/archive")
    ResponseEntity<Void> archive(@PathVariable UUID id) {
        messages.archive(id);
        return ResponseEntity.noContent().build();
    }
}
