package com.personal.marketnote.fulfillment.port.out.goods;

public interface FindFulfillmentGoodsRegistrationPort {
    boolean existsByProductId(Long productId);
}
