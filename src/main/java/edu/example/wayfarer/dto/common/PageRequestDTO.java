package edu.example.wayfarer.dto.common;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Builder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;


public record PageRequestDTO(
        int page,
        int size
) {
    public PageRequestDTO {
        page = Math.max(page, 1);
        size = Math.max(5, Math.min(size, 100));;
    }

    public Pageable getPageable(Sort sort) {
        return PageRequest.of(page -1, size, sort);
    }
}
