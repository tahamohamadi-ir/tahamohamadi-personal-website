package ir.tahamohamadi.blog.post.api.admin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

public record AdminBlogCreateRequest(@NotNull UUID categoryId,@Valid @NotNull BlogTranslationRequest fa,@Valid @NotNull BlogTranslationRequest en,@Size(max=50) List<@NotNull UUID> tagIds) { }
