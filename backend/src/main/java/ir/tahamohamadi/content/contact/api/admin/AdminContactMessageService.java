package ir.tahamohamadi.content.contact.api.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.tahamohamadi.audit.event.AuditEvent;
import ir.tahamohamadi.audit.event.AuditEventRepository;
import ir.tahamohamadi.common.audit.AuthenticatedAuditActor;
import ir.tahamohamadi.content.contact.ContactMessage;
import ir.tahamohamadi.content.contact.ContactMessageRepository;
import ir.tahamohamadi.content.contact.ContactMessageStatus;
import ir.tahamohamadi.identity.user.AppUser;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@ConditionalOnExpression("!'${spring.autoconfigure.exclude:}'.contains('DataSourceAutoConfiguration')")
public class AdminContactMessageService {
    private final ContactMessageRepository messages;
    private final AuditEventRepository audit;
    private final AuthenticatedAuditActor actor;
    private final ObjectMapper mapper;

    public AdminContactMessageService(
            ContactMessageRepository messages,
            AuditEventRepository audit,
            AuthenticatedAuditActor actor,
            ObjectMapper mapper
    ) {
        this.messages = messages;
        this.audit = audit;
        this.actor = actor;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public Page<AdminContactMessageSummary> list(ContactMessageStatus status, Pageable pageable) {
        return messages.findByStatusOrderBySubmittedAtDescIdDesc(status, pageable)
                .map(this::summary);
    }

    @Transactional(readOnly = true)
    public AdminContactMessageDetail get(UUID id) {
        return detail(message(id));
    }

    @Transactional
    public AdminContactMessageStatusResponse markRead(UUID id) {
        AppUser authenticatedActor = actor.required();
        ContactMessage message = lockedMessage(id);
        if (message.markRead(Instant.now())) {
            messages.flush();
            record(authenticatedActor, "ADMIN_CONTACT_MESSAGE_READ", message);
        }
        return status(message);
    }

    @Transactional
    public void archive(UUID id) {
        AppUser authenticatedActor = actor.required();
        ContactMessage message = lockedMessage(id);
        if (message.archive(Instant.now())) {
            messages.flush();
            record(authenticatedActor, "ADMIN_CONTACT_MESSAGE_ARCHIVED", message);
        }
    }

    private ContactMessage message(UUID id) {
        return messages.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Contact message not found"));
    }

    private ContactMessage lockedMessage(UUID id) {
        return messages.findByIdForUpdate(id)
                .orElseThrow(() -> new NoSuchElementException("Contact message not found"));
    }

    private AdminContactMessageSummary summary(ContactMessage message) {
        return new AdminContactMessageSummary(
                message.getId(),
                message.getSenderName(),
                message.getSenderEmail(),
                message.getStatus(),
                message.getSubmittedAt()
        );
    }

    private AdminContactMessageDetail detail(ContactMessage message) {
        return new AdminContactMessageDetail(
                message.getId(),
                message.getSenderName(),
                message.getSenderEmail(),
                message.messageForAdministration(),
                message.getSourceLanguage(),
                message.getStatus(),
                message.getSubmittedAt()
        );
    }

    private AdminContactMessageStatusResponse status(ContactMessage message) {
        return new AdminContactMessageStatusResponse(message.getId(), message.getStatus());
    }

    private void record(AppUser authenticatedActor, String action, ContactMessage message) {
        audit.save(AuditEvent.record(
                UUID.randomUUID(),
                Instant.now(),
                authenticatedActor,
                action,
                "CONTACT_MESSAGE",
                message.getId(),
                "SUCCESS",
                null,
                null,
                mapper.createObjectNode().put("status", message.getStatus().name())
        ));
    }
}
