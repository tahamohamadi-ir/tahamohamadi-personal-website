package ir.tahamohamadi.portfolio.project.api.admin;

import org.springframework.data.domain.*;

import java.util.Set;

final class AdminProjectPaging {
    private AdminProjectPaging() { }

    static Pageable page(int page, int size, String requested) {
        String[] parts = requested.split(",", -1);
        if (parts.length != 2 || !Set.of("updatedAt", "sortOrder", "projectKey").contains(parts[0])) throw new IllegalArgumentException("Unsupported sort");
        try {
            return PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(parts[1]), parts[0]).and(Sort.by(Sort.Direction.ASC, "id")));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Unsupported sort");
        }
    }
}
