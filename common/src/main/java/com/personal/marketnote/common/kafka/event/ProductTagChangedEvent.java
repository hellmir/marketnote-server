package com.personal.marketnote.common.kafka.event;

import java.util.List;

public record ProductTagChangedEvent(
        Long productId,
        List<ProductTagSnapshot> productTags,
        Action action
) {

    public enum Action {
        UPDATED,
        REORDERED
    }

    public record ProductTagSnapshot(
            Long id,
            String name,
            Long orderNum
    ) {
    }
}
