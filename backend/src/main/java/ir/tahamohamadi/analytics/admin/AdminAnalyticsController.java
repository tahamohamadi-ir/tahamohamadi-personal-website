package ir.tahamohamadi.analytics.admin;
import ir.tahamohamadi.blog.post.BlogPostRepository;
import ir.tahamohamadi.content.contact.ContactMessageRepository;
import ir.tahamohamadi.content.contact.ContactMessageStatus;
import ir.tahamohamadi.content.page.ContentPageRepository;
import ir.tahamohamadi.media.asset.MediaAssetRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
@RestController @RequestMapping("/api/v1/admin/analytics") @ConditionalOnExpression("!'${spring.autoconfigure.exclude:}'.contains('DataSourceAutoConfiguration')")
public class AdminAnalyticsController {
    private final ContentPageRepository pages; private final BlogPostRepository posts; private final ContactMessageRepository contacts; private final MediaAssetRepository media;
    public AdminAnalyticsController(ContentPageRepository pages,BlogPostRepository posts,ContactMessageRepository contacts,MediaAssetRepository media){this.pages=pages;this.posts=posts;this.contacts=contacts;this.media=media;}
    @GetMapping("/summary") ResponseEntity<Map<String,Object>> summary(){return ResponseEntity.ok(Map.of("pages",pages.count(),"posts",posts.count(),"media",media.count(),"newContactMessages",contacts.findByStatusOrderBySubmittedAtDescIdDesc(ContactMessageStatus.NEW,org.springframework.data.domain.PageRequest.of(0,1)).getTotalElements()));}
}
