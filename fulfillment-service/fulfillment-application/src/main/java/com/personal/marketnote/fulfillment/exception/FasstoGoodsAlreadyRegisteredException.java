package com.personal.marketnote.fulfillment.exception;

public class FasstoGoodsAlreadyRegisteredException extends RuntimeException {
    public FasstoGoodsAlreadyRegisteredException(Long productId) {
        super("이미 Fassto에 등록된 상품입니다. productId=" + productId);
    }
}
