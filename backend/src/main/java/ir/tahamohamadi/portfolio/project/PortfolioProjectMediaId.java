package ir.tahamohamadi.portfolio.project;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.UUID;

@Embeddable
public record PortfolioProjectMediaId(UUID portfolioProjectId, UUID mediaAssetId) implements Serializable { }
