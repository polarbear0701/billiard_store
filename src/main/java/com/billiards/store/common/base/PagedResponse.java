package com.billiards.store.common.base;

import java.util.List;

import org.springframework.data.domain.Page;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "One page of results with paging information")
public class PagedResponse<T> {
    @Schema(description = "The items of this page")
    private List<T> content;
    @Schema(description = "Zero-based index of this page", example = "0")
    private int page;
    @Schema(description = "Requested page size", example = "20")
    private int size;
    @Schema(description = "Total number of items across all pages", example = "57")
    private long totalElements;
    @Schema(description = "Total number of pages", example = "3")
    private int totalPages;
    @Schema(description = "true if this is the first page", example = "true")
    private boolean first;
    @Schema(description = "true if this is the last page", example = "false")
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
