package ir.tahamohamadi.common.featureflag;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FeatureFlagServiceTest {

    @Test
    void shouldReturnDefaultEnabledFlags() {
        FeatureFlagProperties properties = new FeatureFlagProperties();
        FeatureFlagService service = new FeatureFlagService(properties);

        assertTrue(service.isEnabled("homeV2"));
        assertTrue(service.isEnabled("composerCanvas"));
        assertTrue(service.isEnabled("articleBlockEditor"));
        assertTrue(service.isEnabled("portfolioCaseStudy"));
        assertTrue(service.isEnabled("workflowScheduling"));
        assertFalse(service.isEnabled("unknownFlag"));
    }

    @Test
    void shouldReflectDisabledFlag() {
        FeatureFlagProperties properties = new FeatureFlagProperties();
        properties.setHomeV2Enabled(false);
        FeatureFlagService service = new FeatureFlagService(properties);

        assertFalse(service.isEnabled("home_v2"));
        assertTrue(service.isEnabled("composer_canvas"));
    }
}
