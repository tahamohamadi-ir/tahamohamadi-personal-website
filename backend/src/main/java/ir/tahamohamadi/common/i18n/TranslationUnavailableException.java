package ir.tahamohamadi.common.i18n;

import java.util.List;

public class TranslationUnavailableException extends RuntimeException {
    private final List<String> availableLocales;
    private final List<String> alternatePaths;
    public TranslationUnavailableException(List<String> availableLocales, List<String> alternatePaths) {
        this.availableLocales = List.copyOf(availableLocales);
        this.alternatePaths = List.copyOf(alternatePaths);
    }
    public List<String> availableLocales() { return availableLocales; }
    public List<String> alternatePaths() { return alternatePaths; }
}
