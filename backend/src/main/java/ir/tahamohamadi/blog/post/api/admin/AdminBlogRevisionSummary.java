package ir.tahamohamadi.blog.post.api.admin;
import java.time.Instant; import java.util.UUID;
public record AdminBlogRevisionSummary(UUID id, int revisionNumber, String reason, Instant createdAt, UUID createdBy) { }
