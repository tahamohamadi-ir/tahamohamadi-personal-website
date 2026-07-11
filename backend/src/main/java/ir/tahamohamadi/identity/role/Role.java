package ir.tahamohamadi.identity.role;

import ir.tahamohamadi.identity.user.AppUser;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "role")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Role {

    @Id
    private UUID id;

    @Column(nullable = false, length = 64)
    private String code;

    @Column(length = 500)
    private String description;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", foreignKey = @ForeignKey(name = "fk_role_created_by"))
    private AppUser createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by", foreignKey = @ForeignKey(name = "fk_role_updated_by"))
    private AppUser updatedBy;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deleted_by", foreignKey = @ForeignKey(name = "fk_role_deleted_by"))
    private AppUser deletedBy;

    @Version
    @Column(nullable = false)
    private long version;

    private Role(UUID id, String code, String description, Instant createdAt) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.code = requireStableCode(code);
        this.description = description;
        this.active = true;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
        this.updatedAt = createdAt;
    }

    public static Role create(String code, String description, Instant createdAt) {
        return create(UUID.randomUUID(), code, description, createdAt);
    }

    public static Role create(UUID id, String code, String description, Instant createdAt) {
        return new Role(id, code, description, createdAt);
    }

    private static String requireStableCode(String code) {
        if (code == null || !code.matches("^[A-Z][A-Z0-9_]*$")) {
            throw new IllegalArgumentException("code must be an uppercase stable identifier");
        }
        return code;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Role role)) {
            return false;
        }
        return id != null && id.equals(role.id);
    }

    @Override
    public int hashCode() {
        return id == null ? 0 : id.hashCode();
    }

    @Override
    public String toString() {
        return "Role{id=" + id + ", code='" + code + "', active=" + active + "}";
    }
}
