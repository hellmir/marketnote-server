package com.personal.marketnote.product.exception;

import lombok.Getter;

@Getter
public class ProductTagNotFoundException extends RuntimeException {
    private static final String PRODUCT_TAG_NOT_FOUND_EXCEPTION_MESSAGE =
            "ERR_PRODUCT_TAG_01::상품 태그를 찾을 수 없습니다. tagId=%d, productId=%d";

    public ProductTagNotFoundException(Long tagId, Long productId) {
        super(String.format(PRODUCT_TAG_NOT_FOUND_EXCEPTION_MESSAGE, tagId, productId));
    }
}
