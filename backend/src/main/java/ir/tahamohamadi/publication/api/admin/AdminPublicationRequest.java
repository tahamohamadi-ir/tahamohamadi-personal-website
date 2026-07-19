package ir.tahamohamadi.publication.api.admin;

import ir.tahamohamadi.publication.PublicationStage;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.UUID;

public record AdminPublicationRequest(@NotBlank @Size(max=100) String publicationKey, @NotNull PublicationStage publicationStage, @Pattern(regexp="^$|^10\\.[0-9]{4,9}/[-._;()/:A-Za-z0-9]+$") @Size(max=255) String doi, @Size(max=2048) @Pattern(regexp="^$|^https?://.*$") String externalUrl, LocalDate publishedOn, @Min(1000) @Max(9999) int year, UUID coverMediaId, @Min(0) int sortOrder, @NotNull @Valid AdminPublicationTranslationRequest fa, @NotNull @Valid AdminPublicationTranslationRequest en, Long version) { }
record AdminPublicationTranslationRequest(@NotBlank @Size(max=255) String title, @NotBlank @Size(max=255) String slug, @Size(max=10000) String abstractText, @Size(max=10000) String authorsDisplay, @Size(max=10000) String venueDisplay, @Size(max=255) String seoTitle, @Size(max=500) String seoDescription) { }
