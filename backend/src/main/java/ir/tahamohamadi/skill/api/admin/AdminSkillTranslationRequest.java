package ir.tahamohamadi.skill.api.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminSkillTranslationRequest(
        @NotBlank @Size(max = 255) String name,
        @Size(max = 10000) String description
) { }
