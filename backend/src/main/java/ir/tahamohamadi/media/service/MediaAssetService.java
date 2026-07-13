package ir.tahamohamadi.media.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.tahamohamadi.audit.event.AuditEvent;
import ir.tahamohamadi.audit.event.AuditEventRepository;
import ir.tahamohamadi.common.domain.LanguageCode;
import ir.tahamohamadi.media.api.MediaUploadException;
import ir.tahamohamadi.media.api.admin.MediaAssetResponse;
import ir.tahamohamadi.media.api.admin.MediaAssetSummary;
import ir.tahamohamadi.media.api.admin.MediaMetadataRequest;
import ir.tahamohamadi.media.asset.*;
import ir.tahamohamadi.media.storage.MediaStorage;
import ir.tahamohamadi.media.validation.MediaValidationService;
import ir.tahamohamadi.media.validation.ValidatedMedia;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

@Service @ConditionalOnExpression("!'${spring.autoconfigure.exclude:}'.contains('DataSourceAutoConfiguration')")
public class MediaAssetService {
    private final MediaAssetRepository assets; private final MediaAssetTranslationRepository translations; private final MediaStorage storage;
    private final MediaValidationService validation; private final MediaReferenceService references; private final AuditEventRepository audit; private final ObjectMapper objectMapper;
    public MediaAssetService(MediaAssetRepository assets, MediaAssetTranslationRepository translations, MediaStorage storage, MediaValidationService validation, MediaReferenceService references, AuditEventRepository audit, ObjectMapper objectMapper) { this.assets=assets; this.translations=translations; this.storage=storage; this.validation=validation; this.references=references; this.audit=audit; this.objectMapper=objectMapper; }
    @Transactional
    public MediaAssetResponse upload(MultipartFile file, String faAlt, String faCaption, String enAlt, String enCaption) {
        ValidatedMedia checked = validation.validate(file);
        String key = UUID.randomUUID() + "." + checked.extension();
        try {
            String checksum = checksum(file); storage.store(key, file.getInputStream());
            MediaAsset asset = assets.save(MediaAsset.create(key, checked.originalFilename(), checked.extension(), checked.mimeType(), checked.sizeBytes(), checksum, checked.width(), checked.height(), Instant.now()));
            if (hasText(faAlt)) translations.save(MediaAssetTranslation.create(UUID.randomUUID(), asset, LanguageCode.fa, faAlt.trim(), faCaption, Instant.now()));
            if (hasText(enAlt)) translations.save(MediaAssetTranslation.create(UUID.randomUUID(), asset, LanguageCode.en, enAlt.trim(), enCaption, Instant.now()));
            record("MEDIA_UPLOAD", asset.getId()); return response(asset);
        } catch (IOException | RuntimeException exception) { try { storage.delete(key); } catch (IOException ignored) { } if (exception instanceof RuntimeException runtime) throw runtime; throw new MediaUploadException("Unable to store uploaded file", exception); }
    }
    @Transactional(readOnly = true) public Page<MediaAssetSummary> list(Pageable pageable) { return assets.findByDeletedAtIsNullOrderByUpdatedAtDescIdDesc(pageable).map(a -> new MediaAssetSummary(a.getId(),a.getMimeType(),a.getSizeBytes(),a.getStatus(),a.getCreatedAt(),a.getVersion())); }
    @Transactional(readOnly = true) public MediaAssetResponse get(UUID id) { return response(asset(id)); }
    @Transactional public MediaAssetResponse update(UUID id, MediaMetadataRequest request) { MediaAsset asset=asset(id); if(asset.getVersion()!=request.version()) throw new org.springframework.orm.ObjectOptimisticLockingFailureException(MediaAsset.class,id); updateTranslation(asset,LanguageCode.fa,request.faAlt(),request.faCaption()); updateTranslation(asset,LanguageCode.en,request.enAlt(),request.enCaption()); record("MEDIA_METADATA_UPDATED",id); return response(asset); }
    @Transactional public void archive(UUID id) { MediaAsset asset=asset(id); if(references.isReferenced(id)) throw new IllegalStateException("Referenced media cannot be archived"); asset.archive(); record("MEDIA_ARCHIVED",id); }
    @Transactional(readOnly = true) public MediaAsset activePublic(UUID id) { MediaAsset asset=assets.findByIdAndStatusAndDeletedAtIsNull(id,MediaAssetStatus.ACTIVE).orElseThrow(() -> new java.util.NoSuchElementException("Media not found")); if(!references.isPubliclyReferenced(id)) throw new java.util.NoSuchElementException("Media not found"); return asset; }
    private MediaAsset asset(UUID id) { return assets.findById(id).filter(a->a.getDeletedAt()==null).orElseThrow(() -> new java.util.NoSuchElementException("Media not found")); }
    private void updateTranslation(MediaAsset asset, LanguageCode language, String alt, String caption) { requireAlt(alt); translations.findByMediaAssetIdAndLanguageCodeAndDeletedAtIsNull(asset.getId(),language).ifPresentOrElse(t->t.update(alt.trim(),caption),()->translations.save(MediaAssetTranslation.create(UUID.randomUUID(),asset,language,alt.trim(),caption,Instant.now()))); }
    private MediaAssetResponse response(MediaAsset a) { var fa=translations.findByMediaAssetIdAndLanguageCodeAndDeletedAtIsNull(a.getId(),LanguageCode.fa).orElse(null); var en=translations.findByMediaAssetIdAndLanguageCodeAndDeletedAtIsNull(a.getId(),LanguageCode.en).orElse(null); return new MediaAssetResponse(a.getId(),a.getOriginalFilename(),a.getMimeType(),a.getSizeBytes(),a.getWidth(),a.getHeight(),a.getStatus(),fa==null?null:fa.getAltText(),fa==null?null:fa.getCaption(),en==null?null:en.getAltText(),en==null?null:en.getCaption(),a.getCreatedAt(),a.getVersion()); }
    private String checksum(MultipartFile file) throws IOException { try { MessageDigest digest=MessageDigest.getInstance("SHA-256"); try(var in=file.getInputStream()){ byte[] buffer=new byte[8192]; for(int read;(read=in.read(buffer))!=-1;) digest.update(buffer,0,read); } return HexFormat.of().formatHex(digest.digest()); } catch(java.security.GeneralSecurityException e){throw new IllegalStateException(e);} }
    private void record(String action, UUID target) { audit.save(AuditEvent.record(UUID.randomUUID(),Instant.now(),null,action,"MEDIA_ASSET",target,"SUCCESS",null,null,objectMapper.createObjectNode())); }
    private static void requireAlt(String alt) { if(!hasText(alt)||alt.length()>500) throw MediaUploadException.invalid("Localized alt text is required"); }
    private static boolean hasText(String value) { return value != null && !value.isBlank(); }
}
