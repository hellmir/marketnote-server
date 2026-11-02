package com.personal.marketnote.community.exception;

import lombok.Getter;
import org.springframework.security.access.AccessDeniedException;

@Getter
public class NotProductSellerException extends AccessDeniedException {
    private static final String NOT_PRODUCT_SELLER_EXCEPTION_MESSAGE
            = "상품 판매자가 아닙니다. 전송된 가격 정책 ID: %d";

    public NotProductSellerException(Long pricePolicyId) {
        super(String.format(NOT_PRODUCT_SELLER_EXCEPTION_MESSAGE, pricePolicyId));
    }
}
