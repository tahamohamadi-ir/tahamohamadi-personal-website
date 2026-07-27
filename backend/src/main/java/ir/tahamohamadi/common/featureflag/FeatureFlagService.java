package ir.tahamohamadi.common.featureflag;

import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class FeatureFlagService {
    private final FeatureFlagProperties properties;

    public FeatureFlagService(FeatureFlagProperties properties) {
        this.properties = properties;
    }

    public boolean isEnabled(String flagName) {
        if (flagName == null) return false;
        return switch (flagName.toLowerCase()) {
            case "home_v2", "homev2" -> properties.isHomeV2Enabled();
            case "composer_canvas", "composercanvas" -> properties.isComposerCanvasEnabled();
            case "article_editor", "articleblockeditor" -> properties.isArticleBlockEditorEnabled();
            case "case_study", "portfoliocasestudy" -> properties.isPortfolioCaseStudyEnabled();
            case "workflow_scheduling", "workflowscheduling" -> properties.isWorkflowSchedulingEnabled();
            default -> false;
        };
    }

    public Map<String, Boolean> getAllFlags() {
        return Map.of(
            "homeV2", properties.isHomeV2Enabled(),
            "composerCanvas", properties.isComposerCanvasEnabled(),
            "articleBlockEditor", properties.isArticleBlockEditorEnabled(),
            "portfolioCaseStudy", properties.isPortfolioCaseStudyEnabled(),
            "workflowScheduling", properties.isWorkflowSchedulingEnabled()
        );
    }
}
