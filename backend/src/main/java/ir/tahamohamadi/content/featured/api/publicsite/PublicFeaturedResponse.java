package ir.tahamohamadi.content.featured.api.publicsite;

import java.time.Instant;
import java.util.List;

public record PublicFeaturedResponse(String locale, List<String> availableLocales, String canonicalPath, List<PublicFeaturedLink> hreflang, PublicFeaturedSeo seo, Object ogMedia, Instant lastModified, String slot, List<PublicFeaturedItemResponse> items) { }
record PublicFeaturedLink(String locale, String path) { }
record PublicFeaturedSeo(String title, String description) { }
