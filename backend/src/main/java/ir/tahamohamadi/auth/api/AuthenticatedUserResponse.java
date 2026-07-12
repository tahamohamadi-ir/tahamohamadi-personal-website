package ir.tahamohamadi.auth.api;

import java.util.List;
import java.util.UUID;

public record AuthenticatedUserResponse(UUID id, String displayName, List<String> roles) {

    public AuthenticatedUserResponse {
        roles = List.copyOf(roles);
    }
}
