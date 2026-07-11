package ir.tahamohamadi.identity.user;

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
@Table(name = "app_user")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AppUser {

    @Id
    private UUID id;

    @Column(nullable = false, length = 320)
    private String email;

    @Getter(AccessLevel.NONE)
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "display_name", nullable = false, length = 200)
    private String displayName;

    @Column(nullable = false)
    private boolean enabled;

    @Column(name = "failed_login_count", nullable = false)
    private int failedLoginCount;

    @Column(name = "locked_until")
    private Instant lockedUntil;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", foreignKey = @ForeignKey(name = "fk_app_user_created_by"))
    private AppUser createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by", foreignKey = @ForeignKey(name = "fk_app_user_updated_by"))
    private AppUser updatedBy;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deleted_by", foreignKey = @ForeignKey(name = "fk_app_user_deleted_by"))
    private AppUser deletedBy;

    @Version
    @Column(nullable = false)
    private long version;

    private AppUser(UUID id, String email, String passwordHash, String displayName, Instant createdAt) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.email = normalizeEmail(email);
        this.passwordHash = requireNonBlank(passwordHash, "passwordHash");
        this.displayName = requireNonBlank(displayName, "displayName");
        this.enabled = true;
        this.failedLoginCount = 0;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
        this.updatedAt = createdAt;
    }

    public static AppUser create(String email, String passwordHash, String displayName, Instant createdAt) {
        return create(UUID.randomUUID(), email, passwordHash, displayName, createdAt);
    }

    public static AppUser create(UUID id, String email, String passwordHash, String displayName, Instant createdAt) {
        return new AppUser(id, email, passwordHash, displayName, createdAt);
    }

    public void rename(String displayName) {
        this.displayName = requireNonBlank(displayName, "displayName");
    }

    public void softDelete(AppUser actor, Instant deletedAt) {
        this.deletedBy = actor;
        this.deletedAt = Objects.requireNonNull(deletedAt, "deletedAt must not be null");
    }

    public String passwordHashForAuthentication() {
        return passwordHash;
    }

    private static String normalizeEmail(String email) {
        return requireNonBlank(email, "email").trim();
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
        if (!(other instanceof AppUser appUser)) {
            return false;
        }
        return id != null && id.equals(appUser.id);
    }

    @Override
    public int hashCode() {
        return id == null ? 0 : id.hashCode();
    }

    @Override
    public String toString() {
        return "AppUser{id=" + id + ", email='" + email + "', displayName='" + displayName + "'}";
    }
}
