package com.personal.marketnote.common.kafka.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
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

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ProductTagSnapshot(
            Long id,
            String name,
            Long orderNum
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ProductOptionSnapshot(
            Long id,
            String content
    ) {
    }
}
