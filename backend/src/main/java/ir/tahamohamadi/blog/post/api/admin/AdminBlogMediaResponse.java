package ir.tahamohamadi.blog.post.api.admin;

import java.util.UUID;

public record AdminBlogMediaResponse(UUID mediaAssetId, String usage, int sortOrder) { }
