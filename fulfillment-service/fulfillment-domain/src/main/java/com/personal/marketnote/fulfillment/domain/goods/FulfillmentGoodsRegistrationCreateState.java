package com.personal.marketnote.fulfillment.domain.goods;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FulfillmentGoodsRegistrationCreateState {
    private final Long productId;
}
