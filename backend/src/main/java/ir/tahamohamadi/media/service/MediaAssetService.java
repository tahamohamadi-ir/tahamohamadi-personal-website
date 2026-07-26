package ir.tahamohamadi.media.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.tahamohamadi.audit.event.AuditEvent;
import ir.tahamohamadi.audit.event.AuditEventRepository;
import ir.tahamohamadi.common.audit.AuthenticatedAuditActor;
import ir.tahamohamadi.common.domain.LanguageCode;
import ir.tahamohamadi.identity.user.AppUser;
import ir.tahamohamadi.media.api.MediaUploadException;
import ir.tahamohamadi.media.api.admin.MediaAssetResponse;
import ir.tahamohamadi.media.api.admin.MediaAssetSummary;
import ir.tahamohamadi.media.api.admin.MediaMetadataRequest;
import ir.tahamohamadi.media.api.admin.MediaReplaceRequest;
import ir.tahamohamadi.media.asset.*;
import ir.tahamohamadi.media.storage.MediaStorage;
import ir.tahamohamadi.media.validation.MediaValidationService;
import ir.tahamohamadi.media.validation.ValidatedMedia;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@ConditionalOnExpression("!'${spring.autoconfigure.exclude:}'.contains('DataSourceAutoConfiguration')")
public class MediaAssetService {
    private final MediaAssetRepository assets;
    private final MediaAssetTranslationRepository translations;
    private final MediaStorage storage;
    private final MediaValidationService validation;
    private final MediaReferenceService references;
    private final AuditEventRepository audit;
    private final ObjectMapper objectMapper;
    private final AuthenticatedAuditActor actor;

    public MediaAssetService(
            MediaAssetRepository assets,
            MediaAssetTranslationRepository translations,
            MediaStorage storage,
            MediaValidationService validation,
            MediaReferenceService references,
            AuditEventRepository audit,
            ObjectMapper objectMapper,
            AuthenticatedAuditActor actor
    ) {
        this.assets = assets;
        this.translations = translations;
        this.storage = storage;
        this.validation = validation;
        this.references = references;
        this.audit = audit;
        this.objectMapper = objectMapper;
        this.actor = actor;
    }

    @Transactional
    public MediaAssetResponse upload(
            MultipartFile file,
            String faAlt,
            String faCaption,
            String enAlt,
            String enCaption
    ) {
        AppUser authenticatedActor = actor.required();
        ValidatedMedia checked = validation.validate(file);
        String key = UUID.randomUUID() + "." + checked.extension();

        try {
            String checksum = checksum(file);
            storage.store(key, file.getInputStream());
            MediaAsset asset = assets.save(MediaAsset.create(
                    key,
                    checked.originalFilename(),
                    checked.extension(),
                    checked.mimeType(),
                    checked.sizeBytes(),
                    checksum,
                    checked.width(),
                    checked.height(),
                    Instant.now()
            ));
            if (hasText(faAlt)) {
                translations.save(MediaAssetTranslation.create(
                        UUID.randomUUID(),
                        asset,
                        LanguageCode.fa,
                        faAlt.trim(),
                        faCaption,
                        Instant.now()
                ));
            }
            if (hasText(enAlt)) {
                translations.save(MediaAssetTranslation.create(
                        UUID.randomUUID(),
                        asset,
                        LanguageCode.en,
                        enAlt.trim(),
                        enCaption,
                        Instant.now()
                ));
            }
            assets.flush();
            record(authenticatedActor, "ADMIN_MEDIA_UPLOADED", asset.getId());
            return response(asset);
        } catch (IOException | RuntimeException exception) {
            try {
                storage.delete(key);
            } catch (IOException ignored) {
            }
            if (exception instanceof RuntimeException runtime) {
                throw runtime;
            }
            throw new MediaUploadException("Unable to store uploaded file", exception);
        }
    }

    @Transactional(readOnly = true)
    public Page<MediaAssetSummary> list(
            Pageable pageable,
            String query,
            String mimePrefix,
            MediaAssetStatus status
    ) {
        return assets.findAll(mediaFilter(query, mimePrefix, status), pageable)
                .map(asset -> new MediaAssetSummary(
                        asset.getId(),
                        asset.getOriginalFilename(),
                        asset.getMimeType(),
                        asset.getSizeBytes(),
                        asset.getStatus(),
                        asset.getCreatedAt(),
                        asset.getVersion()
                ));
    }

    @Transactional(readOnly = true)
    public MediaAsset adminReadable(UUID id) {
        return asset(id);
    }

    @Transactional(readOnly = true)
    public MediaAssetResponse get(UUID id) {
        return response(asset(id));
    }

    @Transactional
    public MediaAssetResponse update(UUID id, MediaMetadataRequest request) {
        AppUser authenticatedActor = actor.required();
        MediaAsset asset = asset(id);
        requireVersion(asset, request.version());

        updateTranslation(asset, LanguageCode.fa, request.faAlt(), request.faCaption());
        updateTranslation(asset, LanguageCode.en, request.enAlt(), request.enCaption());
        asset.touch(Instant.now());
        assets.flush();

        record(authenticatedActor, "ADMIN_MEDIA_METADATA_UPDATED", id);
        return response(asset);
    }

    @Transactional
    public void archive(UUID id, long version) {
        AppUser authenticatedActor = actor.required();
        MediaAsset asset = asset(id);
        requireVersion(asset, version);

        if (references.isReferenced(id)) {
            throw new IllegalStateException("Referenced media cannot be archived");
        }

        asset.archive();
        asset.touch(Instant.now());
        assets.flush();
        record(authenticatedActor, "ADMIN_MEDIA_ARCHIVED", id);
    }

    @Transactional
    public MediaAssetResponse replace(UUID id, MediaReplaceRequest request) {
        AppUser authenticatedActor = actor.required();
        MediaAsset source = asset(id);
        requireVersion(source, request.version());
        if (source.getId().equals(request.replacementMediaId())) {
            throw new IllegalArgumentException("Replacement media must be different");
        }
        MediaAsset replacement = assets.findByIdAndStatusAndDeletedAtIsNull(
                        request.replacementMediaId(), MediaAssetStatus.ACTIVE)
                .orElseThrow(() -> new NoSuchElementException("Replacement media not found"));
        if (!compatible(source, replacement)) {
            throw new IllegalArgumentException("Replacement media must have the same media type");
        }
        int usages = references.replaceReferences(source.getId(), replacement.getId());
        source.archive();
        source.touch(Instant.now());
        assets.flush();
        record(authenticatedActor, "ADMIN_MEDIA_REPLACED", id);
        return response(replacement);
    }

    @Transactional(readOnly = true)
    public MediaAsset activePublic(UUID id) {
        MediaAsset asset = assets.findByIdAndStatusAndDeletedAtIsNull(id, MediaAssetStatus.ACTIVE)
                .orElseThrow(() -> new NoSuchElementException("Media not found"));
        if (!references.isPubliclyReferenced(id)) {
            throw new NoSuchElementException("Media not found");
        }
        return asset;
    }

    private MediaAsset asset(UUID id) {
        return assets.findById(id)
                .filter(value -> value.getDeletedAt() == null)
                .orElseThrow(() -> new NoSuchElementException("Media not found"));
    }

    private static Specification<MediaAsset> mediaFilter(
            String query,
            String mimePrefix,
            MediaAssetStatus status
    ) {
        return (root, criteriaQuery, builder) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            predicates.add(builder.isNull(root.get("deletedAt")));
            if (hasText(query)) {
                predicates.add(builder.like(
                        builder.lower(root.get("originalFilename")),
                        "%" + query.trim().toLowerCase(Locale.ROOT) + "%"
                ));
            }
            if (mimePrefix != null) {
                predicates.add(builder.like(root.get("mimeType"), mimePrefix + "%"));
            }
            if (status != null) {
                predicates.add(builder.equal(root.get("status"), status));
            }
            return builder.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
    }

    private static boolean compatible(MediaAsset source, MediaAsset replacement) {
        return source.getMimeType().startsWith("image/") == replacement.getMimeType().startsWith("image/");
    }

    private void updateTranslation(
            MediaAsset asset,
            LanguageCode language,
            String alt,
            String caption
    ) {
        requireAlt(alt);
        translations.findByMediaAssetIdAndLanguageCodeAndDeletedAtIsNull(asset.getId(), language)
                .ifPresentOrElse(
                        translation -> translation.update(alt.trim(), caption),
                        () -> translations.save(MediaAssetTranslation.create(
                                UUID.randomUUID(),
                                asset,
                                language,
                                alt.trim(),
                                caption,
                                Instant.now()
                        ))
                );
    }

    private MediaAssetResponse response(MediaAsset asset) {
        var fa = translations.findByMediaAssetIdAndLanguageCodeAndDeletedAtIsNull(
                asset.getId(),
                LanguageCode.fa
        ).orElse(null);
        var en = translations.findByMediaAssetIdAndLanguageCodeAndDeletedAtIsNull(
                asset.getId(),
                LanguageCode.en
        ).orElse(null);

        return new MediaAssetResponse(
                asset.getId(),
                asset.getOriginalFilename(),
                asset.getMimeType(),
                asset.getSizeBytes(),
                asset.getWidth(),
                asset.getHeight(),
                asset.getStatus(),
                fa == null ? null : fa.getAltText(),
                fa == null ? null : fa.getCaption(),
                en == null ? null : en.getAltText(),
                en == null ? null : en.getCaption(),
                asset.getCreatedAt(),
                asset.getVersion()
        );
    }

    private String checksum(MultipartFile file) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (var input = file.getInputStream()) {
                byte[] buffer = new byte[8192];
                for (int read; (read = input.read(buffer)) != -1; ) {
                    digest.update(buffer, 0, read);
                }
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (java.security.GeneralSecurityException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private void record(AppUser authenticatedActor, String action, UUID target) {
        audit.save(AuditEvent.record(
                UUID.randomUUID(),
                Instant.now(),
                authenticatedActor,
                action,
                "MEDIA_ASSET",
                target,
                "SUCCESS",
                null,
                null,
                objectMapper.createObjectNode().put("changedFields", "managed")
        ));
    }

    private static void requireVersion(MediaAsset asset, long version) {
        if (asset.getVersion() != version) {
            throw new ObjectOptimisticLockingFailureException(MediaAsset.class, asset.getId());
        }
    }

    private static void requireAlt(String alt) {
        if (!hasText(alt) || alt.length() > 500) {
            throw MediaUploadException.invalid("Localized alt text is required");
        }
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
