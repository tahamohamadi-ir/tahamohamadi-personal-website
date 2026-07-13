package ir.tahamohamadi.portfolio.project.api.admin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.*;

public record AdminProjectCreateRequest(
        @NotBlank @Size(max = 100) String projectKey,
        UUID coverMediaId,
        LocalDate startedOn,
        LocalDate endedOn,
        @Size(max = 2048) String projectUrl,
        @Size(max = 2048) String repositoryUrl,
        @Min(0) int sortOrder,
        @Valid @NotNull AdminProjectTranslationRequest fa,
        @Valid @NotNull AdminProjectTranslationRequest en,
        @Valid @NotNull @Size(max = 50) List<@NotNull AdminProjectSkillReferenceRequest> skills
) { }
