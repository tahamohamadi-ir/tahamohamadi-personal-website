package ir.tahamohamadi.blog.post;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.tahamohamadi.audit.event.AuditEvent;
import ir.tahamohamadi.audit.event.AuditEventRepository;
import ir.tahamohamadi.common.domain.ContentStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.UUID;

@Component
public class BlogScheduledPublisher {
    private final BlogPostRepository posts;
    private final AuditEventRepository audit;
    private final ObjectMapper mapper;
    public BlogScheduledPublisher(BlogPostRepository posts, AuditEventRepository audit, ObjectMapper mapper) { this.posts=posts; this.audit=audit; this.mapper=mapper; }

    @Scheduled(fixedDelayString = "${taha.blog.schedule-poll-ms:30000}")
    @Transactional
    public void publishDue() {
        Instant now=Instant.now();
        for (BlogPost post : posts.findByStatusAndScheduledForLessThanEqualAndDeletedAtIsNullOrderByScheduledForAscIdAsc(ContentStatus.SCHEDULED, now)) {
            post.publishScheduled(now);
            audit.save(AuditEvent.record(UUID.randomUUID(),now,null,"SYSTEM_BLOG_POST_SCHEDULED_PUBLISHED","BLOG_POST",post.getId(),"SUCCESS",null,null,mapper.createObjectNode().put("scheduledFor",now.toString())));
        }
    }
}
