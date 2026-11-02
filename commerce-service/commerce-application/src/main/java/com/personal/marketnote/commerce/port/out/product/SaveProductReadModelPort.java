package com.personal.marketnote.commerce.port.out.product;

import java.util.UUID;

public interface SaveProductReadModelPort {

    void upsert(
            Long pricePolicyId, Long productId, UUID productKey, Long sellerId,
            String name, String brandName, Long price, Long discountPrice, Long accumulatedPoint
    );

    void deactivateByPricePolicyId(Long pricePolicyId);
}
