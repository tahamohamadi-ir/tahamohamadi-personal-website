package ir.tahamohamadi.publication.api.admin;
import ir.tahamohamadi.publication.PublicationStage; import java.util.UUID;
public record AdminPublicationSummary(UUID id,String publicationKey,PublicationStage publicationStage,String status,int year,int sortOrder,AdminPublicationTranslationRequest fa,AdminPublicationTranslationRequest en,long version) { }
