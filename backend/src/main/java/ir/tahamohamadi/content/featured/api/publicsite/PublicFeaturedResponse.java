package ir.tahamohamadi.content.featured.api.publicsite;

import java.util.List;

public record PublicFeaturedResponse(String locale, String slot, List<PublicFeaturedItemResponse> items) { }
