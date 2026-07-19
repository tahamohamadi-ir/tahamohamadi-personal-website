package ir.tahamohamadi.portfolio.project.api.admin;

import java.util.UUID;

public record AdminProjectSummary(UUID id, String projectKey, String status, int sortOrder,
                                  AdminProjectTranslationRequest fa, AdminProjectTranslationRequest en, long version) { }
