package com.personal.marketnote.product.exception;

public class CategoryHasProductsException extends IllegalStateException {
    private static final String MESSAGE = "ERR_CATEGORY_PRODUCTS_01::해당 카테고리에 등록된 상품이 존재합니다.";

    public CategoryHasProductsException() {
        super(MESSAGE);
    }
}
