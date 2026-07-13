package ir.tahamohamadi.publicsite.api;
import java.util.List;
public class TranslationUnavailableException extends RuntimeException { private final List<String> availableLocales; private final List<String> alternatePaths; public TranslationUnavailableException(List<String> availableLocales,List<String> alternatePaths){this.availableLocales=availableLocales;this.alternatePaths=alternatePaths;} public List<String> availableLocales(){return availableLocales;} public List<String> alternatePaths(){return alternatePaths;} }
