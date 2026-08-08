package ir.tahamohamadi.media;

import ir.tahamohamadi.media.api.admin.MediaOrphanResponse;
import ir.tahamohamadi.media.asset.MediaAsset;
import ir.tahamohamadi.media.asset.MediaAssetRepository;
import ir.tahamohamadi.media.asset.MediaAssetStatus;
import ir.tahamohamadi.media.service.MediaOrphanReportService;
import ir.tahamohamadi.media.service.MediaReferenceService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MediaOrphanReportServiceUnitTest {
    @Mock MediaAssetRepository assets;
    @Mock MediaReferenceService references;

    @Test
    void returnsAStablePageOfOrphansAfterRefreshingTheUsageIndex() {
        MediaAsset orphan = media();
        PageRequest requested = PageRequest.of(2, 20);

        when(assets.findOrphans("portrait", "image/", "ACTIVE", requested))
                .thenReturn(new PageImpl<>(List.of(orphan), requested, 41));

        Page<MediaOrphanResponse> result = new MediaOrphanReportService(assets, references)
                .findOrphans(requested, "portrait", "image/", MediaAssetStatus.ACTIVE);

        assertThat(result.getContent())
                .extracting(MediaOrphanResponse::id)
                .containsExactly(orphan.getId());
        assertThat(result.getTotalElements()).isEqualTo(41);
        assertThat(result.getNumber()).isEqualTo(2);
        verify(references).refreshUsageIndex();
        verify(assets).findOrphans("portrait", "image/", "ACTIVE", requested);
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
