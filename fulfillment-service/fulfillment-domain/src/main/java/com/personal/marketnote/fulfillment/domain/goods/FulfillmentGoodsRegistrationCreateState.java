package com.personal.marketnote.fulfillment.domain.goods;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class FulfillmentGoodsRegistrationCreateState {
    private final UUID productKey;
}
