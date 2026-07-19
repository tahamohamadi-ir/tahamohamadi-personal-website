package ir.tahamohamadi.portfolio.project.api.admin;

import java.time.LocalDate;
import java.util.*;

public record AdminProjectResponse(
        UUID id,
        String projectKey,
        UUID coverMediaId,
        String status,
        LocalDate startedOn,
        LocalDate endedOn,
        String projectUrl,
        String repositoryUrl,
        int sortOrder,
        AdminProjectTranslationRequest fa,
        AdminProjectTranslationRequest en,
        List<AdminProjectSkillResponse> skills,
        long version
) { }
