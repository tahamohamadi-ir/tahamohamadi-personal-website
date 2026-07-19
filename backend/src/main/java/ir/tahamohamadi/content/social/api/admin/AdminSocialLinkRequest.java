package ir.tahamohamadi.content.social.api.admin;

import jakarta.validation.constraints.*;

public record AdminSocialLinkRequest(
        @NotBlank @Size(max = 64) String platformCode,
        @NotBlank @Size(max = 2048) @Pattern(regexp = "https?://.+", message = "url must be http or https") String url,
        @Min(0) int sortOrder,
        @Min(0) Long version
) { }
