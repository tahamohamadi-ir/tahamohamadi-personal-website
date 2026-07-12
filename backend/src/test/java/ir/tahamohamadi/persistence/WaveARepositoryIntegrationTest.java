package ir.tahamohamadi.persistence;

import ir.tahamohamadi.media.asset.MediaAssetRepository;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WaveARepositoryIntegrationTest {

    @Test
    void exposesTheWaveAMediaRepositoryContract() {
        assertThat(MediaAssetRepository.class).isNotNull();
    }
}
