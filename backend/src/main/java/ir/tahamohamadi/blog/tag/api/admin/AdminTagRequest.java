package ir.tahamohamadi.blog.tag.api.admin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AdminTagRequest(
        @NotBlank @Size(max = 100) String tagKey,
        @Valid @NotNull AdminTagTranslationRequest fa,
        @Valid @NotNull AdminTagTranslationRequest en,
        Long version) { }
