package com.personal.marketnote.product.port.out.event;

public interface PublishProductEventPort {

    void publishProductRegisteredEvent(Long productId, Long pricePolicyId, Long sellerId);
}
