package ir.tahamohamadi.seo;

import java.util.List;

public record SitemapDataResponse(List<SitemapEntryResponse> items) {
    public SitemapDataResponse { items = List.copyOf(items); }
}
