package ir.tahamohamadi.content.page.api.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PageTranslationRequest(
        @NotBlank @Size(max = 255) String title,
        @NotBlank @Size(max = 255) String slug,
        @Size(max = 20_000) String summary,
        @Size(max = 100_000) String bodyMarkdown,
        @Size(max = 255) String seoTitle,
        @Size(max = 500) String seoDescription,
        @Size(max = 500) String canonicalPath) { }
