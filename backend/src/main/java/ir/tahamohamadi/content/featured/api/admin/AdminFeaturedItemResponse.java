package ir.tahamohamadi.content.featured.api.admin;

import java.time.Instant;
import java.util.UUID;

public record AdminFeaturedItemResponse(UUID id, String slotKey, FeaturedTargetType targetType, UUID targetId,
                                        int sortOrder, Instant startsAt, Instant endsAt, boolean active, long version) { }
