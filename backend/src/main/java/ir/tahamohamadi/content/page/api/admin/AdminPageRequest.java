package ir.tahamohamadi.content.page.api.admin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AdminPageRequest(
        @NotBlank @Size(max = 100) String pageKey,
        @Valid @NotNull PageTranslationRequest fa,
        @Valid @NotNull PageTranslationRequest en,
        Long version) { }
