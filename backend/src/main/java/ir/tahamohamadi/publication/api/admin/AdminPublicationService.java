package ir.tahamohamadi.publication.api.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.tahamohamadi.audit.event.AuditEvent;
import ir.tahamohamadi.audit.event.AuditEventRepository;
import ir.tahamohamadi.common.audit.AuthenticatedAuditActor;
import ir.tahamohamadi.common.domain.LanguageCode;
import ir.tahamohamadi.media.asset.MediaAsset;
import ir.tahamohamadi.media.asset.MediaAssetRepository;
import ir.tahamohamadi.media.asset.MediaAssetStatus;
import ir.tahamohamadi.publication.Publication;
import ir.tahamohamadi.publication.PublicationRepository;
import ir.tahamohamadi.publication.PublicationTranslation;
import ir.tahamohamadi.publication.PublicationTranslationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class AdminPublicationService {
    private final PublicationRepository publications;
    private final PublicationTranslationRepository translations;
    private final MediaAssetRepository media;
    private final AuditEventRepository audit;
    private final ObjectMapper mapper;
    private final AuthenticatedAuditActor actor;

    public AdminPublicationService(
            PublicationRepository publications,
            PublicationTranslationRepository translations,
            MediaAssetRepository media,
            AuditEventRepository audit,
            ObjectMapper mapper,
            AuthenticatedAuditActor actor
    ) {
        this.publications = publications;
        this.translations = translations;
        this.media = media;
        this.audit = audit;
        this.mapper = mapper;
        this.actor = actor;
    }

    @Transactional(readOnly = true)
    public Page<AdminPublicationSummary> list(int page, int size, String sort) {
        Pageable pageable = paging(page, size, sort);
        Page<Publication> values = publications.findByDeletedAtIsNull(pageable);
        Map<UUID, List<PublicationTranslation>> byId = translations
                .findByPublicationIdInAndDeletedAtIsNull(values.getContent().stream().map(Publication::getId).toList())
                .stream()
                .collect(Collectors.groupingBy(value -> value.getPublication().getId()));
        return values.map(value -> summary(value, byId.getOrDefault(value.getId(), List.of())));
    }

    @Transactional(readOnly = true)
    public AdminPublicationResponse get(UUID id) {
        return response(publication(id));
    }

    public AdminPublicationResponse create(AdminPublicationRequest request) {
        actor.required();
        Instant now = Instant.now();
        Publication publication = Publication.create(
                UUID.randomUUID(), request.publicationKey(), request.publicationStage(), request.year(), request.sortOrder(), now
        );
        publication.update(
                request.publicationKey(), request.publicationStage(), request.doi(), request.externalUrl(), request.publishedOn(),
                request.year(), cover(request.coverMediaId()), request.sortOrder(), now
        );
        publication = publications.save(publication);
        saveTranslations(publication, request, now);
        publications.flush();
        record("ADMIN_PUBLICATION_CREATED", publication.getId());
        return response(publication);
    }

    public AdminPublicationResponse update(UUID id, AdminPublicationRequest request) {
        Publication publication = publication(id);
        version(publication, request.version());
        Instant now = Instant.now();
        publication.update(
                request.publicationKey(), request.publicationStage(), request.doi(), request.externalUrl(), request.publishedOn(),
                request.year(), cover(request.coverMediaId()), request.sortOrder(), now
        );
        saveTranslations(publication, request, now);
        publications.flush();
        record("ADMIN_PUBLICATION_UPDATED", id);
        return response(publication);
    }

    public AdminPublicationResponse publish(UUID id, long requestedVersion) {
        Publication publication = publication(id);
        version(publication, requestedVersion);
        requireReady(publication);
        publication.publish(Instant.now());
        publications.flush();
        record("ADMIN_PUBLICATION_PUBLISHED", id);
        return response(publication);
    }

    public AdminPublicationResponse archive(UUID id, long requestedVersion) {
        Publication publication = publication(id);
        version(publication, requestedVersion);
        publication.archive(Instant.now());
        publications.flush();
        record("ADMIN_PUBLICATION_ARCHIVED", id);
        return response(publication);
    }

    public void delete(UUID id, long requestedVersion) {
        Publication publication = publication(id);
        version(publication, requestedVersion);
        publication.softDelete(actor.required(), Instant.now());
        publications.flush();
        record("ADMIN_PUBLICATION_DELETED", id);
    }

    private Publication publication(UUID id) {
        return publications.findById(id)
                .filter(value -> value.getDeletedAt() == null)
                .orElseThrow(() -> new NoSuchElementException("Publication not found"));
    }

    private MediaAsset cover(UUID id) {
        if (id == null) return null;
        MediaAsset asset = media.findByIdAndStatusAndDeletedAtIsNull(id, MediaAssetStatus.ACTIVE)
                .orElseThrow(() -> new NoSuchElementException("Media asset not found"));
        if (!asset.getMimeType().startsWith("image/")) {
            throw new IllegalArgumentException("Publication cover media asset must be an image");
        }
        return asset;
    }

    private void saveTranslations(Publication publication, AdminPublicationRequest request, Instant now) {
        List<PublicationTranslation> existing = translations.findByPublicationIdAndDeletedAtIsNull(publication.getId());
        saveTranslation(publication, existing, LanguageCode.fa, request.fa(), now);
        saveTranslation(publication, existing, LanguageCode.en, request.en(), now);
    }

    private void saveTranslation(
            Publication publication,
            List<PublicationTranslation> existing,
            LanguageCode language,
            AdminPublicationTranslationRequest request,
            Instant now
    ) {
        existing.stream().filter(value -> value.getLanguageCode() == language).findFirst().ifPresentOrElse(
                value -> value.update(request.title(), request.slug(), request.abstractText(), request.authorsDisplay(), request.venueDisplay(), request.seoTitle(), request.seoDescription(), now),
                () -> translations.save(PublicationTranslation.create(UUID.randomUUID(), publication, language, request.title(), request.slug(), request.abstractText(), request.authorsDisplay(), request.venueDisplay(), request.seoTitle(), request.seoDescription(), now))
        );
    }

    private void requireReady(Publication publication) {
        if (publication.getPublishedOn() == null || translations.findByPublicationIdAndDeletedAtIsNull(publication.getId()).size() != 2) {
            throw new IllegalArgumentException("Publication requires published date and both translations");
        }
    }

    private AdminPublicationResponse response(Publication publication) {
        List<PublicationTranslation> localized = translations.findByPublicationIdAndDeletedAtIsNull(publication.getId());
        return new AdminPublicationResponse(
                publication.getId(), publication.getPublicationKey(), publication.getPublicationStage(), publication.getContentStatus().name(),
                publication.getDoi(), publication.getExternalUrl(), publication.getPublishedOn(), publication.getYear(),
                publication.getCoverMedia() == null ? null : publication.getCoverMedia().getId(), publication.getSortOrder(),
                translation(localized, LanguageCode.fa), translation(localized, LanguageCode.en), publication.getVersion()
        );
    }

    private AdminPublicationSummary summary(Publication publication, List<PublicationTranslation> localized) {
        return new AdminPublicationSummary(
                publication.getId(), publication.getPublicationKey(), publication.getPublicationStage(), publication.getContentStatus().name(), publication.getYear(), publication.getSortOrder(),
                translation(localized, LanguageCode.fa), translation(localized, LanguageCode.en), publication.getVersion()
        );
    }

    private AdminPublicationTranslationRequest translation(List<PublicationTranslation> values, LanguageCode language) {
        return values.stream().filter(value -> value.getLanguageCode() == language).findFirst()
                .map(value -> new AdminPublicationTranslationRequest(value.getTitle(), value.getSlug(), value.getAbstractText(), value.getAuthorsDisplay(), value.getVenueDisplay(), value.getSeoTitle(), value.getSeoDescription()))
                .orElse(null);
    }

    private static void version(Publication publication, Long requestedVersion) {
        if (requestedVersion == null || publication.getVersion() != requestedVersion) {
            throw new ObjectOptimisticLockingFailureException(Publication.class, publication.getId());
        }
    }

    private static Pageable paging(int page, int size, String requested) {
        String[] parts = requested.split(",", -1);
        if (parts.length != 2 || !Set.of("updatedAt", "year", "sortOrder", "publicationKey").contains(parts[0])) {
            throw new IllegalArgumentException("Unsupported sort");
        }
        try {
            return PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(parts[1]), parts[0]).and(Sort.by("id")));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Unsupported sort");
        }
    }

    private void record(String action, UUID id) {
        audit.save(AuditEvent.record(
                UUID.randomUUID(), Instant.now(), actor.required(), action, "PUBLICATION", id, "SUCCESS", null, null,
                mapper.createObjectNode().put("changedFields", "managed")
        ));
    }
}
