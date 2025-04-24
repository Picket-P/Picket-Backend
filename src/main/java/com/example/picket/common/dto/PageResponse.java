package com.example.picket.common.dto;

import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
public class PageResponse<T> {

    private final List<T> content;

    private final int size;

    private final int page;

    private final long totalElements;

    private final int totalPages;

    private PageResponse(List<T> content, int size, int page, long totalElements, int totalPages) {
        this.content = content;
        this.size = size;
        this.page = page;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
    }

    public static <T> PageResponse<T> of(Page<T> page) {
        return new PageResponse<T>(
                page.getContent(),
                page.getSize(),
                page.getNumber() + 1,
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
