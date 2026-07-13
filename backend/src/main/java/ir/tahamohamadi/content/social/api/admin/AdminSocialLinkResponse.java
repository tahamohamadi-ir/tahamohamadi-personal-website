package ir.tahamohamadi.content.social.api.admin;

import java.util.UUID;

public record AdminSocialLinkResponse(UUID id, String platformCode, String url, int sortOrder, boolean active, long version) { }
