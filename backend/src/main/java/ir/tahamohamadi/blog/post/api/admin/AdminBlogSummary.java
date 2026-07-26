package ir.tahamohamadi.blog.post.api.admin;

import ir.tahamohamadi.common.domain.LanguageCode;

import java.time.Instant;
import java.util.UUID;

public record AdminBlogSummary(
        UUID id,
        UUID categoryId,
        String status,
        LanguageCode sourceLanguage,
        AdminBlogTranslationStatus faTranslationStatus,
        AdminBlogTranslationStatus enTranslationStatus,
        String sourceTitle,
        Instant sourceUpdatedAt,
        long version
) { }
