package ir.tahamohamadi.common.i18n;

import ir.tahamohamadi.common.domain.LanguageCode;

import java.text.Normalizer;

public final class LocaleNormalizer {
    private LocaleNormalizer() { }

    public static String normalizeSearchQuery(LanguageCode locale, String value) {
        if (value == null) return null;
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFC)
                .replace('\u064A', '\u06CC').replace('\u0649', '\u06CC').replace('\u0643', '\u06A9')
                .replace("\u0640", "").replace('\u200C', ' ').replaceAll("\\s+", " ").trim();
        if (normalized.isEmpty()) throw new IllegalArgumentException("Search query must not be blank");
        return normalized;
    }
}
