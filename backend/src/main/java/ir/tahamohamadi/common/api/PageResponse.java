package ir.tahamohamadi.common.api;

import org.springframework.data.domain.Page;
import java.util.List;

public record PageResponse<T>(List<T> items, int page, int size, long totalElements, int totalPages, String sort) {
    public static <T> PageResponse<T> from(Page<T> value) { return new PageResponse<>(value.getContent(), value.getNumber(), value.getSize(), value.getTotalElements(), value.getTotalPages(), value.getSort().toString()); }
}
