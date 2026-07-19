package ir.tahamohamadi.media;

import ir.tahamohamadi.media.api.admin.MediaOrphanResponse;
import ir.tahamohamadi.media.asset.MediaAsset;
import ir.tahamohamadi.media.asset.MediaAssetRepository;
import ir.tahamohamadi.media.service.MediaOrphanReportService;
import ir.tahamohamadi.media.service.MediaReferenceService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MediaOrphanReportServiceUnitTest {
    @Mock MediaAssetRepository assets;
    @Mock MediaReferenceService references;

    @Test
    void resolvesReferencesInOneBatchInsteadOfOneQueryPerAsset() {
        MediaAsset referenced = media();
        MediaAsset orphan = media();

        when(assets.findByDeletedAtIsNullOrderByUpdatedAtDescIdDesc(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(referenced, orphan)));
        when(references.referencedIds(anyCollection()))
                .thenReturn(Set.of(referenced.getId()));

        List<MediaOrphanResponse> result =
                new MediaOrphanReportService(assets, references).findOrphans();

        assertThat(result)
                .extracting(MediaOrphanResponse::id)
                .containsExactly(orphan.getId());
        verify(references).referencedIds(anyCollection());
        verify(references, never()).isReferenced(any(UUID.class));
    }

    private static MediaAsset media() {
        return MediaAsset.create(
                UUID.randomUUID(),
                UUID.randomUUID() + ".png",
                "photo.png",
                "png",
                "image/png",
                1,
                "a".repeat(64),
                1,
                1,
                Instant.now()
        );
    }
}
