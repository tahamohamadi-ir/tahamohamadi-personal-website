package ir.tahamohamadi.resume.api.admin;
import ir.tahamohamadi.common.domain.LanguageCode; import java.time.Instant; import java.util.UUID;
public record AdminResumeDocumentResponse(UUID id,LanguageCode languageCode,UUID mediaAssetId,String status,Instant publishedAt,long version) { }
