package ir.tahamohamadi.seo;

public record SeoMetadataResponse(String title, String description, OpenGraphResponse openGraph) {
    public SeoMetadataResponse(String title, String description) { this(title, description, new OpenGraphResponse(title, description, null)); }
    public record OpenGraphResponse(String title, String description, String imageUrl) { }
}
