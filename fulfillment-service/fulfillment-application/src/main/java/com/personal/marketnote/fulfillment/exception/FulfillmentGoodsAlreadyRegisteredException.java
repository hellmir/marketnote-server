package com.personal.marketnote.fulfillment.exception;

public class FulfillmentGoodsAlreadyRegisteredException extends RuntimeException {
    public FulfillmentGoodsAlreadyRegisteredException(Long productId) {
        super("이미 Fulfillment에 등록된 상품입니다. productId=" + productId);
    }
}
