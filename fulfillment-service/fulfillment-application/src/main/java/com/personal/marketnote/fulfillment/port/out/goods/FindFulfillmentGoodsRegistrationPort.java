package com.personal.marketnote.fulfillment.port.out.goods;

import java.util.UUID;

public interface FindFulfillmentGoodsRegistrationPort {
    boolean existsByProductKey(UUID productKey);
}
