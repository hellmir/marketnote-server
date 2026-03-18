package com.personal.marketnote.product.exception;

import jakarta.persistence.EntityNotFoundException;

public class ShippingPolicyNotFoundException extends EntityNotFoundException {

    public ShippingPolicyNotFoundException(Long sellerId) {
        super("해당 판매자의 배송비 정책을 찾을 수 없습니다. sellerId=" + sellerId);
    }
}
