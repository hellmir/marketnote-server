package com.personal.marketnote.product.exception;

public class ShippingPolicyAlreadyExistsException extends IllegalStateException {

    public ShippingPolicyAlreadyExistsException(Long sellerId) {
        super("해당 판매자의 배송비 정책이 이미 존재합니다. sellerId=" + sellerId);
    }
}
