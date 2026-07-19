package ir.tahamohamadi.resume.api.admin;
import ir.tahamohamadi.resume.ResumeEntryType; import jakarta.validation.Valid; import jakarta.validation.constraints.*; import java.time.LocalDate;
public record AdminResumeEntryRequest(@NotNull ResumeEntryType entryType,@NotNull LocalDate startedOn,LocalDate endedOn,boolean current,@Min(0) int sortOrder,@NotNull @Valid AdminResumeTranslationRequest fa,@NotNull @Valid AdminResumeTranslationRequest en,Long version) { }
record AdminResumeTranslationRequest(@NotBlank @Size(max=255) String title,@NotBlank @Size(max=255) String organization,@Size(max=255) String location,@Size(max=10000) String summary) { }
