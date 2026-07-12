package ir.tahamohamadi.media.api.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MediaMetadataRequest(@NotNull Long version,
                                   @NotBlank @Size(max = 500) String faAlt, @Size(max = 2000) String faCaption,
                                   @NotBlank @Size(max = 500) String enAlt, @Size(max = 2000) String enCaption) { }
