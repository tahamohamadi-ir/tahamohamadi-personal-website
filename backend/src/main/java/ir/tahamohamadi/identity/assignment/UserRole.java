package ir.tahamohamadi.identity.assignment;

import ir.tahamohamadi.identity.role.Role;
import ir.tahamohamadi.identity.user.AppUser;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "user_role")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserRole {

    @EmbeddedId
    private UserRoleId id;

    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_user_role_user"))
    private AppUser user;

    @MapsId("roleId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_id", foreignKey = @ForeignKey(name = "fk_user_role_role"))
    private Role role;

    @Column(name = "assigned_at", nullable = false)
    private Instant assignedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by", foreignKey = @ForeignKey(name = "fk_user_role_assigned_by"))
    private AppUser assignedBy;

    private UserRole(AppUser user, Role role, AppUser assignedBy, Instant assignedAt) {
        this.user = Objects.requireNonNull(user, "user must not be null");
        this.role = Objects.requireNonNull(role, "role must not be null");
        this.id = new UserRoleId(user.getId(), role.getId());
        this.assignedBy = assignedBy;
        this.assignedAt = Objects.requireNonNull(assignedAt, "assignedAt must not be null");
    }

    public static UserRole assign(AppUser user, Role role, AppUser assignedBy, Instant assignedAt) {
        return new UserRole(user, role, assignedBy, assignedAt);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof UserRole userRole)) {
            return false;
        }
        return id != null && id.equals(userRole.id);
    }

    @Override
    public int hashCode() {
        return id == null ? 0 : id.hashCode();
    }

    @Override
    public String toString() {
        return "UserRole{id=" + id + ", assignedAt=" + assignedAt + "}";
    }
}
