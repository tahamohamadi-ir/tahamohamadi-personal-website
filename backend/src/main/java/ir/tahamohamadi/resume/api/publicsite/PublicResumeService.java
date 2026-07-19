package ir.tahamohamadi.resume.api.publicsite;

import ir.tahamohamadi.common.domain.ContentStatus;
import ir.tahamohamadi.common.domain.LanguageCode;
import ir.tahamohamadi.resume.ResumeDocument;
import ir.tahamohamadi.resume.ResumeDocumentRepository;
import ir.tahamohamadi.resume.ResumeEntry;
import ir.tahamohamadi.resume.ResumeEntryTranslation;
import ir.tahamohamadi.resume.ResumeEntryTranslationRepository;
import ir.tahamohamadi.resume.ResumeEntryType;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class PublicResumeService {
    private final ResumeEntryTranslationRepository entries;
    private final ResumeDocumentRepository documents;

    public PublicResumeService(ResumeEntryTranslationRepository entries, ResumeDocumentRepository documents) {
        this.entries = entries;
        this.documents = documents;
    }

    public PublicResumeResponse get(LanguageCode locale) {
        List<PublicResumeEntryResponse> values = entries.findPublishedByLanguage(locale, PageRequest.of(0, 50, Sort.by(Sort.Order.asc("entry.sortOrder"), Sort.Order.desc("entry.startedOn"), Sort.Order.asc("entry.id")))).map(this::entry).getContent();
        PublicResumeDocumentResponse document = documents.findByLanguageCodeAndStatusAndDeletedAtIsNull(locale, ContentStatus.PUBLISHED).map(this::document).orElse(null);
        String path = "/" + locale + "/resume";
        return new PublicResumeResponse(locale.name(), List.of(locale.name()), path, List.of(new PublicResumeLink(locale.name(), path)), new PublicResumeSeo(null, null), null, null, values, document);
    }

    private PublicResumeEntryResponse entry(ResumeEntryTranslation translation) {
        ResumeEntry entry = translation.getEntry();
        return new PublicResumeEntryResponse(translation.getTitle(), translation.getOrganization(), translation.getLocation(), translation.getSummary(), entry.getEntryType(), entry.getStartedOn(), entry.getEndedOn(), entry.isCurrent());
    }

    private PublicResumeDocumentResponse document(ResumeDocument document) {
        return new PublicResumeDocumentResponse("/api/v1/public/media/" + document.getMediaAsset().getId(), document.getPublishedAt());
    }
}

record PublicResumeResponse(String locale, List<String> availableLocales, String canonicalPath, List<PublicResumeLink> hreflang, PublicResumeSeo seo, Object ogMedia, Instant lastModified, List<PublicResumeEntryResponse> entries, PublicResumeDocumentResponse document) { }
record PublicResumeEntryResponse(String title, String organization, String location, String summary, ResumeEntryType entryType, LocalDate startedOn, LocalDate endedOn, boolean current) { }
record PublicResumeDocumentResponse(String mediaUrl, Instant publishedAt) { }
record PublicResumeLink(String locale, String path) { }
record PublicResumeSeo(String title, String description) { }
