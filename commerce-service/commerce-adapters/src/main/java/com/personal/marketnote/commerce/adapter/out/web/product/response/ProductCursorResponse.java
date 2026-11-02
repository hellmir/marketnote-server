package com.personal.marketnote.commerce.adapter.out.web.product.response;

import java.util.List;

public record ProductCursorResponse<T>(
        Long totalElements,
        Boolean hasNext,
        Long nextCursor,
        List<T> contents
) {
}
