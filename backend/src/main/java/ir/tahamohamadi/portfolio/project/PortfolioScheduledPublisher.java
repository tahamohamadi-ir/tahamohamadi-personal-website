package ir.tahamohamadi.portfolio.project;

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
public class PortfolioScheduledPublisher {
    private final PortfolioProjectRepository projects;
    private final PortfolioProjectTranslationRepository translations;
    private final AuditEventRepository audit;
    private final ObjectMapper mapper;

    public PortfolioScheduledPublisher(PortfolioProjectRepository projects, PortfolioProjectTranslationRepository translations, AuditEventRepository audit, ObjectMapper mapper) {
        this.projects = projects;
        this.translations = translations;
        this.audit = audit;
        this.mapper = mapper;
    }

    @Scheduled(fixedDelayString = "${taha.portfolio.schedule-poll-ms:30000}")
    @Transactional
    public void publishDue() {
        Instant now = Instant.now();
        for (PortfolioProject project : projects.findByStatusAndScheduledForLessThanEqualAndDeletedAtIsNullOrderByScheduledForAscIdAsc(ContentStatus.SCHEDULED, now)) {
            if (!publishable(project)) {
                audit.save(AuditEvent.record(UUID.randomUUID(), now, null, "SYSTEM_PROJECT_SCHEDULED_PUBLISH_FAILED", "PORTFOLIO_PROJECT", project.getId(), "FAILURE", null, null, mapper.createObjectNode().put("reason", "PUBLISH_VALIDATION_FAILED")));
                continue;
            }
            Instant scheduledFor = project.getScheduledFor();
            project.publishScheduled(now);
            audit.save(AuditEvent.record(UUID.randomUUID(), now, null, "SYSTEM_PROJECT_SCHEDULED_PUBLISHED", "PORTFOLIO_PROJECT", project.getId(), "SUCCESS", null, null, mapper.createObjectNode().put("scheduledFor", scheduledFor.toString())));
        }
    }

    private boolean publishable(PortfolioProject project) {
        if (project.getStartedOn() == null) return false;
        List<PortfolioProjectTranslation> values = translations.findByProjectIdAndDeletedAtIsNull(project.getId());
        return java.util.Arrays.stream(LanguageCode.values()).allMatch(language -> values.stream().filter(value -> value.getLanguageCode() == language).anyMatch(value -> nonBlank(value.getSeoTitle()) && nonBlank(value.getSeoDescription())));
    }
    private static boolean nonBlank(String value) { return value != null && !value.isBlank(); }
}
