package ir.tahamohamadi.blog.post.api.admin;

import java.util.List;
import java.util.UUID;

public record AdminBlogResponse(UUID id,UUID categoryId,String status,BlogTranslationRequest fa,BlogTranslationRequest en,List<UUID> tagIds,List<AdminBlogMediaResponse> media,long version) { }
