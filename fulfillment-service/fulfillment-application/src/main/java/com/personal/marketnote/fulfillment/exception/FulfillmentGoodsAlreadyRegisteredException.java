package com.personal.marketnote.fulfillment.exception;

import java.util.UUID;

public class FulfillmentGoodsAlreadyRegisteredException extends RuntimeException {
    public FulfillmentGoodsAlreadyRegisteredException(UUID productKey) {
        super("이미 Fulfillment에 등록된 상품입니다. productKey=" + productKey);
    }
}
