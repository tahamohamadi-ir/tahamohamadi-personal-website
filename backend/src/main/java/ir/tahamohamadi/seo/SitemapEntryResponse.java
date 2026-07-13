package ir.tahamohamadi.seo;

import java.time.Instant;

public record SitemapEntryResponse(String locale, String canonicalPath, Instant lastModified) { }
