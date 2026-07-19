package ir.tahamohamadi.publication.api.admin;
import ir.tahamohamadi.publication.PublicationStage; import java.time.LocalDate; import java.util.UUID;
public record AdminPublicationResponse(UUID id,String publicationKey,PublicationStage publicationStage,String status,String doi,String externalUrl,LocalDate publishedOn,int year,UUID coverMediaId,int sortOrder,AdminPublicationTranslationRequest fa,AdminPublicationTranslationRequest en,long version) { }
