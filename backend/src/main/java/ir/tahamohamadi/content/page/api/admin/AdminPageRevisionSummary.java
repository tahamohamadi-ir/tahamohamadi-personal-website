package ir.tahamohamadi.content.page.api.admin;

import java.time.Instant;
import java.util.UUID;

public record AdminPageRevisionSummary(UUID id, int revisionNumber, String reason, Instant createdAt, UUID createdBy) { }
