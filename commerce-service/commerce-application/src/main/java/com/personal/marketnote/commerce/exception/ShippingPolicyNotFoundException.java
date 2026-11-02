package com.personal.marketnote.commerce.exception;

import com.personal.marketnote.common.domain.exception.DomainNotFoundException;

public class ShippingPolicyNotFoundException extends DomainNotFoundException {

    public ShippingPolicyNotFoundException(Long sellerId) {
        super("해당 판매자의 배송비 정책을 찾을 수 없습니다. sellerId=" + sellerId);
    }
}
