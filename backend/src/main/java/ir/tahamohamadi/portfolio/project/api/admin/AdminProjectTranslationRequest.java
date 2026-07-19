package ir.tahamohamadi.portfolio.project.api.admin;

import jakarta.validation.constraints.*;

public record AdminProjectTranslationRequest(
        @NotBlank @Size(max = 255) String title,
        @NotBlank @Size(max = 255) String slug,
        @Size(max = 10_000) String summary,
        @Size(max = 100_000) String bodyMarkdown,
        @Size(max = 255) String seoTitle,
        @Size(max = 500) String seoDescription
) { }
