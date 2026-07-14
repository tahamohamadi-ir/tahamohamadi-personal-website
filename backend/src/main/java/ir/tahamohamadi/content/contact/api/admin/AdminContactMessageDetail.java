package ir.tahamohamadi.content.contact.api.admin;

import ir.tahamohamadi.common.domain.LanguageCode;
import ir.tahamohamadi.content.contact.ContactMessageStatus;

import java.time.Instant;
import java.util.UUID;

public record AdminContactMessageDetail(
        UUID id,
        String senderName,
        String senderEmail,
        String message,
        LanguageCode language,
        ContactMessageStatus status,
        Instant submittedAt
) {
}
