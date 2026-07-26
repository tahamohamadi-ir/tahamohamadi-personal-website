package ir.tahamohamadi.content.page.api.admin;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AdminPageRevisionResponse(UUID id, int revisionNumber, String reason, Instant createdAt, UUID createdBy, AdminPageRevisionSnapshot snapshot) { }
record AdminPageRevisionSnapshot(AdminPageResponse page, List<PageSectionSnapshot> sections) { }
record PageSectionSnapshot(String type, String layout, boolean enabled, String settingsJson, List<PageBlockSnapshot> blocks) { }
record PageBlockSnapshot(String type, boolean enabled, String settingsJson, BlockTranslationSnapshot fa, BlockTranslationSnapshot en) { }
record BlockTranslationSnapshot(String title, String eyebrow, String lead, String bodyMarkdown, String actionLabel, String actionPath, String alt) { }
