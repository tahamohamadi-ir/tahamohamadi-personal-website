package ir.tahamohamadi.skill.api.admin;

import java.util.UUID;

public record AdminSkillCategoryResponse(
        UUID id, String categoryKey, int sortOrder, boolean active,
        AdminSkillTranslationRequest fa, AdminSkillTranslationRequest en, long version
) { }
