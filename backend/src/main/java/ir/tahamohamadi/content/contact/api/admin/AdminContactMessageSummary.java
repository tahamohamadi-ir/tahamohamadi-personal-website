package ir.tahamohamadi.content.contact.api.admin;

import ir.tahamohamadi.content.contact.ContactMessageStatus;

import java.time.Instant;
import java.util.UUID;

public record AdminContactMessageSummary(
        UUID id,
        String senderName,
        String senderEmail,
        ContactMessageStatus status,
        Instant submittedAt
) {
}
