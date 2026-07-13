package ir.tahamohamadi.blog.post.api.admin;

import ir.tahamohamadi.blog.post.BlogPostMediaUsage;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AdminBlogMediaReferenceRequest(@NotNull UUID mediaAssetId, @NotNull BlogPostMediaUsage usage, @Min(0) int sortOrder) { }
