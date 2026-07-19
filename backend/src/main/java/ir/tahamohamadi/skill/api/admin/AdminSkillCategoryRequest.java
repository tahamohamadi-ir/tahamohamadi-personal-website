package ir.tahamohamadi.skill.api.admin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AdminSkillCategoryRequest(
        @NotBlank @Size(max = 100) String categoryKey,
        @Min(0) int sortOrder,
        @Valid @NotNull AdminSkillTranslationRequest fa,
        @Valid @NotNull AdminSkillTranslationRequest en,
        Long version
) { }
