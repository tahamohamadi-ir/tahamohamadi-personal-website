package ir.tahamohamadi.seo;

import org.springframework.core.env.Environment;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Arrays;

@RestController
public class RobotsController {
    private final Environment environment;
    public RobotsController(Environment environment) { this.environment = environment; }
    @GetMapping(value = "/robots.txt", produces = MediaType.TEXT_PLAIN_VALUE)
    ResponseEntity<String> robots() {
        if (Arrays.stream(environment.getActiveProfiles()).anyMatch(profile -> profile.equals("prod") || profile.equals("production"))) {
            return ResponseEntity.ok().cacheControl(CacheControl.noCache()).body("User-agent: *\nAllow: /\nDisallow: /admin\nDisallow: /api\n");
        }
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).header("X-Robots-Tag", "noindex, nofollow").body("User-agent: *\nDisallow: /\n");
    }
}
