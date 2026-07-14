package ir.tahamohamadi.content.contact.api.publicsite;

import ir.tahamohamadi.content.contact.ContactMessage;
import ir.tahamohamadi.content.contact.ContactMessageRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@ConditionalOnExpression("!'${spring.autoconfigure.exclude:}'.contains('DataSourceAutoConfiguration')")
public class PublicContactService {
    private final ContactMessageRepository messages;

    public PublicContactService(ContactMessageRepository messages) {
        this.messages = messages;
    }

    @Transactional
    public ContactSubmissionResponse submit(ContactSubmissionRequest request, String remoteAddress) {
        Instant submittedAt = Instant.now();
        UUID id = UUID.randomUUID();
        messages.save(ContactMessage.submit(
                id,
                request.name(),
                request.email(),
                request.message(),
                request.language(),
                submittedAt
        ));
        return new ContactSubmissionResponse(id, "RECEIVED", submittedAt);
    }
}
