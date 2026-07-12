package ir.tahamohamadi.seo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
@RestController
public class PublicRobotsController {
    @Value("${spring.profiles.active:local}") private String profile;
    @GetMapping(value="/robots.txt",produces=MediaType.TEXT_PLAIN_VALUE)
    ResponseEntity<String> robots(){String body="prod".equals(profile)?"User-agent: *\nDisallow: /admin\nDisallow: /api\n":"User-agent: *\nDisallow: /\n";return ResponseEntity.ok().cacheControl(CacheControl.noCache()).body(body);}
}
