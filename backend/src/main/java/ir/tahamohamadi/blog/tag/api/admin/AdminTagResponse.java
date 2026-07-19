package ir.tahamohamadi.blog.tag.api.admin;

import java.util.UUID;

public record AdminTagResponse(UUID id, String tagKey, boolean active,
                               AdminTagTranslationRequest fa, AdminTagTranslationRequest en, long version) { }
