package com.personal.marketnote.common.kafka.event;

import java.util.List;

public record PricePolicyCreatedEvent(
        Long productId,
        Long pricePolicyId,
        Long sellerId,
        String productName,
        String brandName,
        Long price,
        Long discountPrice,
        Long accumulatedPoint,
        Long sales,
        List<ProductTagSnapshot> productTags,
        List<ProductOptionSnapshot> selectedOptions
) {

    public record ProductTagSnapshot(
            Long id,
            String name,
            Long orderNum
    ) {
    }

    public record ProductOptionSnapshot(
            Long id,
            String content
    ) {
    }
}
