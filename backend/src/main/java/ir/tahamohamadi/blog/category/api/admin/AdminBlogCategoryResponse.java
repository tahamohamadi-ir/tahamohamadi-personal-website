package ir.tahamohamadi.blog.category.api.admin;

import java.util.UUID;

public record AdminBlogCategoryResponse(UUID id, String categoryKey, int sortOrder, boolean active,
                                        AdminCategoryTranslationRequest fa, AdminCategoryTranslationRequest en, long version) { }
