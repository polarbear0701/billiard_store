package com.billiards.store.common.base;

import java.util.List;

import org.springframework.data.domain.Page;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Generic wrapper for paginated data.
 * Carries the page content along with pagination metadata so clients can
 * render pagination controls without inspecting Spring's {@link Page} type directly.
 *
 * @param <T> the type of the elements contained in the page
 */
@Getter
@Setter
@AllArgsConstructor
public class PagedResponse<T> {
    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean first;
    private boolean last;

    /**
     * Builds a {@link PagedResponse} from a Spring Data {@link Page}.
     *
     * @param page the source page
     * @param <T>  the type of the elements contained in the page
     * @return a populated {@link PagedResponse}
     */
    public static <T> PagedResponse<T> from(Page<T> page) {
        return new PagedResponse<>(
            page.getContent(),
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.isFirst(),
            page.isLast()
        );
    }
}
