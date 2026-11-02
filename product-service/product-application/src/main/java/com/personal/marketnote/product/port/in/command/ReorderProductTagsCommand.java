package com.personal.marketnote.product.port.in.command;

import lombok.Builder;

import java.util.List;

@Builder
public record ReorderProductTagsCommand(
        Long productId,
        List<TagOrderItem> tagOrders
) {
    @Builder
    public record TagOrderItem(Long tagId, Long orderNum) {
    }
}
