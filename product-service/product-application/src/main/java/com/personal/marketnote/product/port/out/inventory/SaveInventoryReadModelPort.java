package com.personal.marketnote.product.port.out.inventory;

public interface SaveInventoryReadModelPort {
    void upsert(Long pricePolicyId, Long productId, Integer stockQuantity);
}
