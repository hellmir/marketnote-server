package com.personal.marketnote.fulfillment.port.out.goods;

public interface FindFasstoGoodsRegistrationPort {
    boolean existsByProductId(Long productId);
}
