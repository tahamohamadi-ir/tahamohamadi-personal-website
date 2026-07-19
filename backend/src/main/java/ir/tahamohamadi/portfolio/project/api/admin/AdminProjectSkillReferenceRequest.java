package ir.tahamohamadi.portfolio.project.api.admin;

import jakarta.validation.constraints.*;
import java.util.UUID;

public record AdminProjectSkillReferenceRequest(@NotNull UUID skillId, @Min(0) int sortOrder) { }
