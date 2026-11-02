package com.personal.marketnote.common.adapter.in.response;

import java.util.List;

public record CursorResponse<T>(
        Long totalElements,
        boolean hasNext,
        Long nextCursor,
        List<T> items
) {
}
