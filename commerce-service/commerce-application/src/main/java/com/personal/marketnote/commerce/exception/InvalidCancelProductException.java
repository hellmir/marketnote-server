package com.personal.marketnote.commerce.exception;

public class InvalidCancelProductException extends IllegalArgumentException {
    private static final String PRODUCT_NOT_IN_ORDER_MESSAGE
            = "ERR_PAYMENT_CANCEL_PRODUCT_01::주문에 존재하지 않는 상품입니다.";
    private static final String DUPLICATE_PRODUCT_MESSAGE
            = "ERR_PAYMENT_CANCEL_PRODUCT_02::중복된 상품이 포함되어 있습니다.";
    private static final String INVALID_QUANTITY_MESSAGE
            = "ERR_PAYMENT_CANCEL_PRODUCT_03::취소 수량은 1 이상이어야 합니다.";
    private static final String QUANTITY_EXCEEDS_ORDER_MESSAGE
            = "ERR_PAYMENT_CANCEL_PRODUCT_04::취소 수량이 주문 수량을 초과합니다.";

    private InvalidCancelProductException(String message) {
        super(message);
    }

    public static InvalidCancelProductException productNotInOrder() {
        return new InvalidCancelProductException(PRODUCT_NOT_IN_ORDER_MESSAGE);
    }

    public static InvalidCancelProductException duplicateProduct() {
        return new InvalidCancelProductException(DUPLICATE_PRODUCT_MESSAGE);
    }

    public static InvalidCancelProductException invalidQuantity() {
        return new InvalidCancelProductException(INVALID_QUANTITY_MESSAGE);
    }

    public static InvalidCancelProductException quantityExceedsOrder() {
        return new InvalidCancelProductException(QUANTITY_EXCEEDS_ORDER_MESSAGE);
    }
}
