package com.personal.marketnote.fulfillment.port.out.goods;

import com.personal.marketnote.fulfillment.domain.goods.FulfillmentGoodsRegistration;

public interface SaveFulfillmentGoodsRegistrationPort {
    void save(FulfillmentGoodsRegistration registration);
}
