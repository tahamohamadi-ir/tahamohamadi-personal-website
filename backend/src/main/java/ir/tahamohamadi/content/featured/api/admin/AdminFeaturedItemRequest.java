package ir.tahamohamadi.content.featured.api.admin;

import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.UUID;

public record AdminFeaturedItemRequest(
        @NotBlank @Size(max = 100) String slotKey,
        @NotNull FeaturedTargetType targetType,
        @NotNull UUID targetId,
        @Min(0) int sortOrder,
        Instant startsAt,
        Instant endsAt,
        @Min(0) Long version
) { }
