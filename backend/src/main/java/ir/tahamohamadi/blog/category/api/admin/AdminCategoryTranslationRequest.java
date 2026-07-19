package ir.tahamohamadi.blog.category.api.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminCategoryTranslationRequest(
        @NotBlank @Size(max = 255) String name,
        @NotBlank @Size(max = 255) String slug,
        @Size(max = 255) String seoTitle,
        @Size(max = 500) String seoDescription) { }
