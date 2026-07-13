package ir.tahamohamadi.content.contact.api.publicsite;

import java.time.Instant;
import java.util.UUID;

/** Intentionally excludes submitted contact content and the sender email. */
public record ContactSubmissionResponse(UUID id, String status, Instant submittedAt) { }
