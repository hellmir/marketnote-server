package com.personal.marketnote.product.exception;

import com.personal.marketnote.common.domain.exception.DomainNotFoundException;
import lombok.Getter;

@Getter
public class ProductNotFoundException extends DomainNotFoundException {
    private static final String PRODUCT_NOT_FOUND_EXCEPTION_MESSAGE = "상품을 찾을 수 없습니다. 전송된 상품 ID: %d";

    public ProductNotFoundException(Long productId) {
        super(String.format(PRODUCT_NOT_FOUND_EXCEPTION_MESSAGE, productId));
    }
}
