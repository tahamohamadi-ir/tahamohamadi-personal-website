package ir.tahamohamadi.portfolio.project.api.admin;

import java.time.LocalDate;
import java.time.Instant;
import java.util.*;

public record AdminProjectResponse(
        UUID id,
        String projectKey,
        UUID coverMediaId,
        String status,
        Instant scheduledFor,
        LocalDate startedOn,
        LocalDate endedOn,
        String projectUrl,
        String repositoryUrl,
        int sortOrder,
        AdminProjectTranslationRequest fa,
        AdminProjectTranslationRequest en,
        List<AdminProjectSkillResponse> skills,
        List<AdminProjectMediaResponse> gallery,
        long version
) { }
