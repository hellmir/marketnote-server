package com.personal.marketnote.product.port.out.event;

import com.personal.marketnote.common.kafka.event.PricePolicyCreatedEvent;
import com.personal.marketnote.common.kafka.event.ProductRegisteredEvent;
import com.personal.marketnote.common.kafka.event.ProductTagChangedEvent;
import com.personal.marketnote.common.kafka.event.ProductUpdatedEvent;

import java.util.List;
import java.util.UUID;

public interface PublishProductEventPort {

    void publishProductRegisteredEvent(
            Long productId, UUID productKey, Long pricePolicyId, Long sellerId, String productName, String goodsType,
            String brandName, Long price, Long discountPrice, Long accumulatedPoint,
            Long sales,
            List<ProductRegisteredEvent.ProductTagSnapshot> productTags,
            List<ProductRegisteredEvent.ProductOptionSnapshot> selectedOptions
    );

    void publishPricePolicyCreatedEvent(PricePolicyCreatedEvent event);

    void publishPricePolicyDeletedEvent(Long productId, Long pricePolicyId);

    void publishProductUpdatedEvent(ProductUpdatedEvent event);

    void publishProductDeletedEvent(Long productId);

    void publishProductTagChangedEvent(
            Long productId,
            List<ProductTagChangedEvent.ProductTagSnapshot> productTags,
            ProductTagChangedEvent.Action action
    );
}
