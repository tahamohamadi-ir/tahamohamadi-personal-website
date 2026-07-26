package ir.tahamohamadi.content.page.api.admin;

import ir.tahamohamadi.common.audit.AuthenticatedAuditActor;
import ir.tahamohamadi.audit.event.AuditEvent;
import ir.tahamohamadi.audit.event.AuditEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.sql.Timestamp;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.UUID;

@Service
@ConditionalOnExpression("!'${spring.autoconfigure.exclude:}'.contains('DataSourceAutoConfiguration')")
public class PagePreviewTokenService {
    private final JdbcTemplate jdbc;
    private final AuthenticatedAuditActor actor;
    private final AuditEventRepository audit;
    private final ObjectMapper mapper;
    private final SecureRandom random = new SecureRandom();

    public PagePreviewTokenService(JdbcTemplate jdbc, AuthenticatedAuditActor actor, AuditEventRepository audit, ObjectMapper mapper) { this.jdbc = jdbc; this.actor = actor; this.audit = audit; this.mapper = mapper; }

    @Transactional
    public IssuedToken issue(UUID pageId) {
        if (jdbc.queryForList("select 1 from content_page where id=? and deleted_at is null", Integer.class, pageId).isEmpty()) throw new java.util.NoSuchElementException("Page not found");
        byte[] bytes = new byte[32]; random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        Instant expiresAt = Instant.now().plus(10, ChronoUnit.MINUTES);
        jdbc.update("delete from content_page_preview_token where content_page_id=? or expires_at<=current_timestamp", pageId);
        var issuedBy = actor.required();
        Instant now = Instant.now();
        jdbc.update("insert into content_page_preview_token (id,content_page_id,token_hash,expires_at,created_at,created_by) values (?,?,?,?,?,?)", UUID.randomUUID(), pageId, hash(token), Timestamp.from(expiresAt), Timestamp.from(now), issuedBy.getId());
        audit.save(AuditEvent.record(UUID.randomUUID(), now, issuedBy, "ADMIN_PAGE_PREVIEW_TOKEN_ISSUED", "PAGE", pageId, "SUCCESS", null, null, mapper.createObjectNode().put("expiresAt", expiresAt.toString())));
        return new IssuedToken(token, expiresAt);
    }

    @Transactional(readOnly = true)
    public void requireValid(UUID pageId, String token) {
        if (token == null || token.isBlank() || jdbc.queryForList("""
                select 1
                from content_page_preview_token token
                join content_page page on page.id = token.content_page_id and page.deleted_at is null
                where token.content_page_id=? and token.token_hash=? and token.expires_at>current_timestamp
                """, Integer.class, pageId, hash(token)).isEmpty()) throw new java.util.NoSuchElementException("Preview not found");
    }

    private static String hash(String token) {
        try { return java.util.HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8))); }
        catch (java.security.NoSuchAlgorithmException exception) { throw new IllegalStateException(exception); }
    }

    public record IssuedToken(String token, Instant expiresAt) { }
}
