package ir.tahamohamadi.identity.assignment;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserRoleId implements Serializable {

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "role_id")
    private UUID roleId;

    public UserRoleId(UUID userId, UUID roleId) {
        this.userId = Objects.requireNonNull(userId, "userId must not be null");
        this.roleId = Objects.requireNonNull(roleId, "roleId must not be null");
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof UserRoleId userRoleId)) {
            return false;
        }
        return userId.equals(userRoleId.userId) && roleId.equals(userRoleId.roleId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, roleId);
    }
}
