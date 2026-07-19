package ir.tahamohamadi.skill.api.admin;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Set;

final class AdminSkillPaging {
    private AdminSkillPaging() { }

    static Pageable page(int page, int size, String requested, Set<String> allowed) {
        String[] parts = requested.split(",", -1);
        if (parts.length != 2 || !allowed.contains(parts[0])) throw new IllegalArgumentException("Unsupported sort");
        Sort.Direction direction;
        try { direction = Sort.Direction.fromString(parts[1]); }
        catch (IllegalArgumentException exception) { throw new IllegalArgumentException("Unsupported sort"); }
        return PageRequest.of(page, size, Sort.by(direction, parts[0]).and(Sort.by(Sort.Direction.ASC, "id")));
    }
}
