package ir.tahamohamadi.audit.event;

import com.fasterxml.jackson.databind.JsonNode;
import ir.tahamohamadi.identity.user.AppUser;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.net.InetAddress;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Immutable
@Table(name = "audit_event")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuditEvent {

    @Id
    private UUID id;

    @Column(name = "occurred_at", nullable = false, updatable = false)
    private Instant occurredAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_user_id", foreignKey = @ForeignKey(name = "fk_audit_event_actor_user"))
    private AppUser actor;

    @Column(nullable = false, length = 100, updatable = false)
    private String action;

    @Column(name = "target_type", nullable = false, length = 100, updatable = false)
    private String targetType;

    @Column(name = "target_id", updatable = false)
    private UUID targetId;

    @Column(nullable = false, length = 20, updatable = false)
    private String outcome;

    @Column(name = "request_id", length = 128, updatable = false)
    private String requestId;

    @JdbcTypeCode(SqlTypes.INET)
    @Column(name = "ip_address", columnDefinition = "inet", updatable = false)
    private InetAddress ipAddress;

    @Getter(AccessLevel.NONE)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb", updatable = false)
    private JsonNode details;

    private AuditEvent(
            UUID id,
            Instant occurredAt,
            AppUser actor,
            String action,
            String targetType,
            UUID targetId,
            String outcome,
            String requestId,
            InetAddress ipAddress,
            JsonNode details
    ) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.occurredAt = Objects.requireNonNull(occurredAt, "occurredAt must not be null");
        this.actor = actor;
        this.action = requireNonBlank(action, "action");
        this.targetType = requireNonBlank(targetType, "targetType");
        this.targetId = targetId;
        this.outcome = requireNonBlank(outcome, "outcome");
        this.requestId = requestId == null ? null : requireNonBlank(requestId, "requestId");
        this.ipAddress = ipAddress;
        this.details = requireObject(details).deepCopy();
    }

    public static AuditEvent record(
            UUID id,
            Instant occurredAt,
            AppUser actor,
            String action,
            String targetType,
            UUID targetId,
            String outcome,
            String requestId,
            InetAddress ipAddress,
            JsonNode details
    ) {
        return new AuditEvent(
                id,
                occurredAt,
                actor,
                action,
                targetType,
                targetId,
                outcome,
                requestId,
                ipAddress,
                details
        );
    }

    public JsonNode getDetails() {
        return details.deepCopy();
    }

    private static JsonNode requireObject(JsonNode details) {
        if (details == null || !details.isObject()) {
            throw new IllegalArgumentException("details must be a JSON object");
        }
        return details;
    }

    private static String requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof AuditEvent auditEvent)) {
            return false;
        }
        return id != null && id.equals(auditEvent.id);
    }

    @Override
    public int hashCode() {
        return id == null ? 0 : id.hashCode();
    }

    @Override
    public String toString() {
        return "AuditEvent{id=" + id + ", occurredAt=" + occurredAt + ", action='" + action
                + "', targetType='" + targetType + "', outcome='" + outcome + "'}";
    }
}
