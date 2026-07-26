package ir.tahamohamadi.blog.post;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.tahamohamadi.audit.event.AuditEvent;
import ir.tahamohamadi.audit.event.AuditEventRepository;
import ir.tahamohamadi.common.domain.ContentStatus;
import ir.tahamohamadi.common.domain.LanguageCode;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.UUID;
import java.util.List;

@Component
public class BlogScheduledPublisher {
    private final BlogPostRepository posts;
    private final BlogPostTranslationRepository translations;
    private final AuditEventRepository audit;
    private final ObjectMapper mapper;
    public BlogScheduledPublisher(BlogPostRepository posts, BlogPostTranslationRepository translations, AuditEventRepository audit, ObjectMapper mapper) { this.posts=posts; this.translations=translations; this.audit=audit; this.mapper=mapper; }

    @Scheduled(fixedDelayString = "${taha.blog.schedule-poll-ms:30000}")
    @Transactional
    public void publishDue() {
        Instant now=Instant.now();
        for (BlogPost post : posts.findByStatusAndScheduledForLessThanEqualAndDeletedAtIsNullOrderByScheduledForAscIdAsc(ContentStatus.SCHEDULED, now)) {
            if (!publishable(post)) {
                audit.save(AuditEvent.record(UUID.randomUUID(),now,null,"SYSTEM_BLOG_POST_SCHEDULED_PUBLISH_FAILED","BLOG_POST",post.getId(),"FAILURE",null,null,mapper.createObjectNode().put("reason","PUBLISH_VALIDATION_FAILED")));
                continue;
            }
            post.publishScheduled(now);
            audit.save(AuditEvent.record(UUID.randomUUID(),now,null,"SYSTEM_BLOG_POST_SCHEDULED_PUBLISHED","BLOG_POST",post.getId(),"SUCCESS",null,null,mapper.createObjectNode().put("scheduledFor",now.toString())));
        }
    }

    private boolean publishable(BlogPost post) {
        if (post.getCategory().getDeletedAt() != null || !post.getCategory().isActive()) return false;
        List<BlogPostTranslation> values = translations.findByBlogPostIdAndDeletedAtIsNull(post.getId());
        return java.util.Arrays.stream(LanguageCode.values()).allMatch(language -> values.stream().filter(value -> value.getLanguageCode() == language).anyMatch(value -> nonBlank(value.getSeoTitle()) && nonBlank(value.getSeoDescription())));
    }
    private static boolean nonBlank(String value) { return value != null && !value.isBlank(); }
}
