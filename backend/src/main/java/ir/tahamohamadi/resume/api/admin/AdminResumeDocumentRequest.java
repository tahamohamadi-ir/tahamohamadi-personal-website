package ir.tahamohamadi.resume.api.admin;
import ir.tahamohamadi.common.domain.LanguageCode; import jakarta.validation.constraints.*; import java.util.UUID;
public record AdminResumeDocumentRequest(@NotNull LanguageCode languageCode,@NotNull UUID mediaAssetId,Long version) { }
