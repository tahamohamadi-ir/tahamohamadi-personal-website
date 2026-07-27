package ir.tahamohamadi.portfolio.project;

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
public class PortfolioScheduledPublisher {
    private final PortfolioProjectRepository projects;
    private final AuditEventRepository audit;
    private final ObjectMapper mapper;

    public PortfolioScheduledPublisher(PortfolioProjectRepository projects, AuditEventRepository audit, ObjectMapper mapper) {
        this.projects = projects;
        this.audit = audit;
        this.mapper = mapper;
    }

    @Scheduled(fixedDelayString = "${taha.portfolio.schedule-poll-ms:30000}")
    @Transactional
    public void publishDue() {
        Instant now = Instant.now();
        for (PortfolioProject project : projects.findByStatusAndScheduledForLessThanEqualAndDeletedAtIsNullOrderByScheduledForAscIdAsc(ContentStatus.SCHEDULED, now)) {
            if (project.getStartedOn() == null) {
                audit.save(AuditEvent.record(UUID.randomUUID(), now, null, "SYSTEM_PROJECT_SCHEDULED_PUBLISH_FAILED", "PORTFOLIO_PROJECT", project.getId(), "FAILURE", null, null, mapper.createObjectNode().put("reason", "PUBLISH_VALIDATION_FAILED")));
                continue;
            }
            Instant scheduledFor = project.getScheduledFor();
            project.publishScheduled(now);
            audit.save(AuditEvent.record(UUID.randomUUID(), now, null, "SYSTEM_PROJECT_SCHEDULED_PUBLISHED", "PORTFOLIO_PROJECT", project.getId(), "SUCCESS", null, null, mapper.createObjectNode().put("scheduledFor", scheduledFor.toString())));
        }
    }
}
