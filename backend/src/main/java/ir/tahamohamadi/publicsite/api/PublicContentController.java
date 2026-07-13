package ir.tahamohamadi.publicsite.api;

import ir.tahamohamadi.common.domain.LanguageCode;
import ir.tahamohamadi.common.i18n.LocaleNormalizer;
import jakarta.validation.constraints.*;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import java.time.Duration;
import java.util.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;

@RestController @Validated @RequestMapping("/api/v1/public/{lang}") @ConditionalOnExpression("!'${spring.autoconfigure.exclude:}'.contains('DataSourceAutoConfiguration')")
public class PublicContentController {
    private final PublicContentService content;
    public PublicContentController(PublicContentService content){this.content=content;}
    @GetMapping("/home") ResponseEntity<?> home(@PathVariable LanguageCode lang){return cached(content.home(lang));}
    @GetMapping("/pages/{slug}") ResponseEntity<?> page(@PathVariable LanguageCode lang,@PathVariable String slug){return cached(content.page(lang,slug));}
    @GetMapping("/posts") ResponseEntity<?> posts(@PathVariable LanguageCode lang,@RequestParam(required=false) @Size(max=256) String q,@RequestParam(required=false) String category,@RequestParam(required=false) String tag,@RequestParam(defaultValue="0") @Min(0) int page,@RequestParam(defaultValue="20") @Min(1) @Max(50) int size){return cached(content.posts(lang,q == null ? null : LocaleNormalizer.normalizeSearchQuery(lang,q),category,tag,page,size));}
    @GetMapping("/posts/{slug}") ResponseEntity<?> post(@PathVariable LanguageCode lang,@PathVariable @NotBlank String slug){return cached(content.post(lang,slug));}
    @GetMapping("/categories") ResponseEntity<?> categories(@PathVariable LanguageCode lang){return cached(content.categories(lang));}
    @GetMapping("/tags") ResponseEntity<?> tags(@PathVariable LanguageCode lang){return cached(content.tags(lang));}
    @GetMapping("/portfolio") ResponseEntity<?> projects(@PathVariable LanguageCode lang,@RequestParam(required=false) String skill,@RequestParam(defaultValue="0") @Min(0) int page,@RequestParam(defaultValue="20") @Min(1) @Max(50) int size){return cached(content.projects(lang,skill,page,size));}
    @GetMapping("/portfolio/{slug}") ResponseEntity<?> project(@PathVariable LanguageCode lang,@PathVariable @NotBlank String slug){return cached(content.project(lang,slug));}
    @GetMapping("/skills") ResponseEntity<?> skills(@PathVariable LanguageCode lang){return cached(content.skills(lang));}
    private static ResponseEntity<?> cached(Object body){return ResponseEntity.ok().cacheControl(CacheControl.maxAge(Duration.ofMinutes(5)).cachePublic()).body(body);}
}
