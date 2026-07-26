package ir.tahamohamadi.media.api.admin;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record MediaReplaceRequest(@NotNull UUID replacementMediaId, @NotNull Long version) { }
