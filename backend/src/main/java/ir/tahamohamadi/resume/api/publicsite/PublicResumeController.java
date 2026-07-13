package ir.tahamohamadi.resume.api.publicsite;
import ir.tahamohamadi.common.domain.LanguageCode; import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/public/{locale}/resume") public class PublicResumeController {private final PublicResumeService service;public PublicResumeController(PublicResumeService service){this.service=service;}@GetMapping public PublicResumeResponse get(@PathVariable LanguageCode locale){return service.get(locale);}}
