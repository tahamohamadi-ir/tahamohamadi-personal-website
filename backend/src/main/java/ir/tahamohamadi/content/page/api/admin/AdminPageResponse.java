package ir.tahamohamadi.content.page.api.admin;

import java.time.Instant;
import java.util.UUID;

public record AdminPageResponse(UUID id, String pageKey, String status, Instant publishedAt,
                                PageTranslationRequest fa, PageTranslationRequest en, long version) { }
