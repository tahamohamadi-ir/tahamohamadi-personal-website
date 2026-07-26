package ir.tahamohamadi.media.api.admin;

import java.util.UUID;

public record MediaUsageResponse(String ownerType, UUID ownerId, String lifecycleStatus) { }
