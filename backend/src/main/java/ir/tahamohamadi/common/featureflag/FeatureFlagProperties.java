package ir.tahamohamadi.common.featureflag;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "taha.features")
public class FeatureFlagProperties {
    private boolean homeV2Enabled = true;
    private boolean composerCanvasEnabled = true;
    private boolean articleBlockEditorEnabled = true;
    private boolean portfolioCaseStudyEnabled = true;
    private boolean workflowSchedulingEnabled = true;

    public boolean isHomeV2Enabled() {
        return homeV2Enabled;
    }

    public void setHomeV2Enabled(boolean homeV2Enabled) {
        this.homeV2Enabled = homeV2Enabled;
    }

    public boolean isComposerCanvasEnabled() {
        return composerCanvasEnabled;
    }

    public void setComposerCanvasEnabled(boolean composerCanvasEnabled) {
        this.composerCanvasEnabled = composerCanvasEnabled;
    }

    public boolean isArticleBlockEditorEnabled() {
        return articleBlockEditorEnabled;
    }

    public void setArticleBlockEditorEnabled(boolean articleBlockEditorEnabled) {
        this.articleBlockEditorEnabled = articleBlockEditorEnabled;
    }

    public boolean isPortfolioCaseStudyEnabled() {
        return portfolioCaseStudyEnabled;
    }

    public void setPortfolioCaseStudyEnabled(boolean portfolioCaseStudyEnabled) {
        this.portfolioCaseStudyEnabled = portfolioCaseStudyEnabled;
    }

    public boolean isWorkflowSchedulingEnabled() {
        return workflowSchedulingEnabled;
    }

    public void setWorkflowSchedulingEnabled(boolean workflowSchedulingEnabled) {
        this.workflowSchedulingEnabled = workflowSchedulingEnabled;
    }
}
