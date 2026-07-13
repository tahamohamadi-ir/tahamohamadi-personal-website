package ir.tahamohamadi.publication.api.publicsite;

import ir.tahamohamadi.common.domain.LanguageCode;
import ir.tahamohamadi.publication.PublicationStage;
import ir.tahamohamadi.publication.PublicationTranslation;
import ir.tahamohamadi.publication.PublicationTranslationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class PublicPublicationService {
    private final PublicationTranslationRepository translations;

    public PublicPublicationService(PublicationTranslationRepository translations) {
        this.translations = translations;
    }

    public PublicPublicationListResponse list(LanguageCode locale, int page, int size, PublicationStage stage) {
        Page<PublicationTranslation> values = translations.findPublishedByLanguage(locale, stage, PageRequest.of(page, size, Sort.by(Sort.Order.desc("publication.year"), Sort.Order.asc("publication.sortOrder"), Sort.Order.asc("publication.id"))));
        Map<UUID, List<PublicationTranslation>> alternatives = alternatives(values.getContent());
        String path = "/" + locale + "/publications";
        return new PublicPublicationListResponse(locale.name(), List.of(locale.name()), path, List.of(new PublicLink(locale.name(), path)), new PublicSeo(null, null), null, null, values.getContent().stream().map(value -> dto(value, alternatives.get(value.getPublication().getId()))).toList(), page, size, values.getTotalElements(), values.getTotalPages());
    }

    public PublicPublicationResponse get(LanguageCode locale, String slug) {
        PublicationTranslation value = translations.findPublishedByLanguageAndSlug(locale, slug).orElseThrow(() -> new NoSuchElementException("Publication not found"));
        return dto(value, translations.findByPublicationIdAndDeletedAtIsNull(value.getPublication().getId()));
    }

    private Map<UUID, List<PublicationTranslation>> alternatives(List<PublicationTranslation> values) {
        if (values.isEmpty()) return Map.of();
        return translations.findByPublicationIdInAndDeletedAtIsNull(values.stream().map(value -> value.getPublication().getId()).toList()).stream().collect(Collectors.groupingBy(value -> value.getPublication().getId()));
    }

    private PublicPublicationResponse dto(PublicationTranslation translation, List<PublicationTranslation> alternatives) {
        List<PublicLink> links = alternatives.stream().map(value -> new PublicLink(value.getLanguageCode().name(), "/" + value.getLanguageCode() + "/publications/" + value.getSlug())).sorted(java.util.Comparator.comparing(PublicLink::locale)).toList();
        var publication = translation.getPublication();
        return new PublicPublicationResponse(translation.getLanguageCode().name(), links.stream().map(PublicLink::locale).toList(), "/" + translation.getLanguageCode() + "/publications/" + translation.getSlug(), links, new PublicSeo(translation.getSeoTitle(), translation.getSeoDescription()), null, translation.getUpdatedAt(), translation.getSlug(), translation.getTitle(), translation.getAbstractText(), translation.getAuthorsDisplay(), translation.getVenueDisplay(), publication.getPublicationStage(), publication.getYear(), publication.getPublishedOn(), publication.getDoi(), publication.getExternalUrl());
    }
}

record PublicPublicationListResponse(String locale, List<String> availableLocales, String canonicalPath, List<PublicLink> hreflang, PublicSeo seo, Object ogMedia, Instant lastModified, List<PublicPublicationResponse> items, int page, int size, long totalElements, int totalPages) { }
record PublicPublicationResponse(String locale, List<String> availableLocales, String canonicalPath, List<PublicLink> hreflang, PublicSeo seo, Object ogMedia, Instant lastModified, String slug, String title, String abstractText, String authorsDisplay, String venueDisplay, PublicationStage stage, int year, LocalDate publishedOn, String doi, String externalUrl) { }
record PublicLink(String locale, String path) { }
record PublicSeo(String title, String description) { }
