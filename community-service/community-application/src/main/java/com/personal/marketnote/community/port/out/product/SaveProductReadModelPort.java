package com.personal.marketnote.community.port.out.product;

public interface SaveProductReadModelPort {

    void upsert(Long pricePolicyId, Long productId, Long sellerId, String name, String brandName,
                Long price, Long discountPrice, Long accumulatedPoint);

    void deactivateByPricePolicyId(Long pricePolicyId);
}
