package ir.tahamohamadi.content.contact.api.publicsite;
import ir.tahamohamadi.common.domain.LanguageCode;
import jakarta.validation.constraints.*;
public record ContactSubmissionRequest(@NotBlank @Size(max=200) String name,@NotBlank @Email @Size(max=320) String email,@NotBlank @Size(max=10000) String message,@NotNull LanguageCode language) { }
