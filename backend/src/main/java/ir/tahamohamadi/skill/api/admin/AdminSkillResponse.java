package ir.tahamohamadi.skill.api.admin;

import java.util.UUID;

public record AdminSkillResponse(
        UUID id, UUID categoryId, String skillKey, int sortOrder, boolean active,
        AdminSkillTranslationRequest fa, AdminSkillTranslationRequest en, long version
) { }
