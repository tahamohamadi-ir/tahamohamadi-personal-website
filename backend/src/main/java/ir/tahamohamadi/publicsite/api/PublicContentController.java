package ir.tahamohamadi.publicsite.api;

import ir.tahamohamadi.common.domain.LanguageCode;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.Duration;
import java.util.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;

@RestController @RequestMapping("/api/v1/public/{lang}") @ConditionalOnExpression("!'${spring.autoconfigure.exclude:}'.contains('DataSourceAutoConfiguration')")
public class PublicContentController {
    private final PublicContentService content;
    public PublicContentController(PublicContentService content){this.content=content;}
    @GetMapping("/pages/{slug}") ResponseEntity<Map<String,Object>> page(@PathVariable LanguageCode lang,@PathVariable String slug){return cached(content.page(lang,slug));}
    @GetMapping("/posts") ResponseEntity<Map<String,Object>> posts(@PathVariable LanguageCode lang,@RequestParam(required=false) String q,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size){check(page,size);if(q!=null&&q.length()>256)throw new IllegalArgumentException("Search query is too long");return cached(Map.of("items",content.posts(lang,q,page,size),"page",page,"size",size));}
    @GetMapping("/posts/{slug}") ResponseEntity<Map<String,Object>> post(@PathVariable LanguageCode lang,@PathVariable String slug){return cached(content.post(lang,slug));}
    @GetMapping("/portfolio") ResponseEntity<Map<String,Object>> projects(@PathVariable LanguageCode lang){return cached(Map.of("items",content.projects(lang)));}
    @GetMapping("/portfolio/{slug}") ResponseEntity<Map<String,Object>> project(@PathVariable LanguageCode lang,@PathVariable String slug){return cached(content.project(lang,slug));}
    @GetMapping("/publications") ResponseEntity<Map<String,Object>> publications(@PathVariable LanguageCode lang){return cached(Map.of("items",content.publications(lang)));}
    @GetMapping("/publications/{slug}") ResponseEntity<Map<String,Object>> publication(@PathVariable LanguageCode lang,@PathVariable String slug){return cached(content.publication(lang,slug));}
    @GetMapping("/resume") ResponseEntity<Map<String,Object>> resume(@PathVariable LanguageCode lang){return cached(Map.of("items",content.resume(lang)));}
    @GetMapping("/social-links") ResponseEntity<Map<String,Object>> social(@PathVariable LanguageCode lang){return cached(Map.of("items",content.social()));}
    private static ResponseEntity<Map<String,Object>> cached(Map<String,Object> body){return ResponseEntity.ok().cacheControl(CacheControl.maxAge(Duration.ofMinutes(5)).cachePublic()).body(body);}
    private static void check(int page,int size){if(page<0||size<1||size>50)throw new IllegalArgumentException("Invalid pagination");}
}
