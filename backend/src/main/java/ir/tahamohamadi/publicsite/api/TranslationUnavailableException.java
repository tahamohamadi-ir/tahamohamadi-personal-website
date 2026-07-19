package ir.tahamohamadi.publicsite.api;

import java.util.List;

public class TranslationUnavailableException extends ir.tahamohamadi.common.i18n.TranslationUnavailableException {
    public TranslationUnavailableException(List<String> availableLocales, List<String> alternatePaths) {
        super(availableLocales, alternatePaths);
    }
}
