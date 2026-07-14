package ir.tahamohamadi.content.contact.api.admin;

import ir.tahamohamadi.content.contact.ContactMessageStatus;

import java.util.UUID;

public record AdminContactMessageStatusResponse(
        UUID id,
        ContactMessageStatus status
) {
}
