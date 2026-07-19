package ir.tahamohamadi.resume.api.admin;
import ir.tahamohamadi.resume.ResumeEntryType; import java.time.LocalDate; import java.util.UUID;
public record AdminResumeEntryResponse(UUID id,ResumeEntryType entryType,String status,LocalDate startedOn,LocalDate endedOn,boolean current,int sortOrder,AdminResumeTranslationRequest fa,AdminResumeTranslationRequest en,long version) { }
