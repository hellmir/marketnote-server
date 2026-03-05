package com.personal.marketnote.product.port.out.event;

public interface PublishProductEventPort {

    void publishProductRegisteredEvent(Long productId, Long pricePolicyId, Long sellerId, String productName, String godType);

    void publishPricePolicyCreatedEvent(Long productId, Long pricePolicyId);
}
