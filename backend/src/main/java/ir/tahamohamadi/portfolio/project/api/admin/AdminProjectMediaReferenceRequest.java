package ir.tahamohamadi.portfolio.project.api.admin;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AdminProjectMediaReferenceRequest(@NotNull UUID mediaAssetId, @Min(0) int sortOrder) { }
