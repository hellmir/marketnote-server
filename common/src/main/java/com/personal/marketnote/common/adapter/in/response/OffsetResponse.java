package com.personal.marketnote.common.adapter.in.response;

import java.util.List;

public record OffsetResponse<T>(
        int page,
        int pageSize,
        long totalElements,
        int totalPages,
        List<T> items
) {
}
