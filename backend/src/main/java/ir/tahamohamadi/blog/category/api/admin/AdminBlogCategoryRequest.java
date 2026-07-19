package ir.tahamohamadi.blog.category.api.admin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AdminBlogCategoryRequest(
        @NotBlank @Size(max = 100) String categoryKey,
        @Min(0) @Max(100000) int sortOrder,
        @Valid @NotNull AdminCategoryTranslationRequest fa,
        @Valid @NotNull AdminCategoryTranslationRequest en,
        Long version) { }
