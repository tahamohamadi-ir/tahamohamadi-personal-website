package ir.tahamohamadi.publicapi;

import ir.tahamohamadi.common.domain.LanguageCode;
import ir.tahamohamadi.common.i18n.LocaleNormalizer;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocaleNormalizerUnitTest {
    @Test
    void normalizesPersianArabicLetterVariantsAndWhitespaceDeterministically() {
        assertThat(LocaleNormalizer.normalizeSearchQuery(LanguageCode.fa, "  كتاب\u200c  يكي  "))
                .isEqualTo("کتاب یکی");
        assertThat(LocaleNormalizer.normalizeSearchQuery(LanguageCode.en, "  Spring   Boot "))
                .isEqualTo("Spring Boot");
    }

    @Test
    void rejectsBlankSearchQueriesInsteadOfSendingAnEmptyFtsExpression() {
        assertThatThrownBy(() -> LocaleNormalizer.normalizeSearchQuery(LanguageCode.fa, " \u200c "))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
