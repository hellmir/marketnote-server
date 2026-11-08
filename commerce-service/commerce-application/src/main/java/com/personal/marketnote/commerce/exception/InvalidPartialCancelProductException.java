package com.personal.marketnote.commerce.exception;

public class InvalidPartialCancelProductException extends IllegalArgumentException {
    private static final String DUPLICATE_PRICE_POLICY_MESSAGE
            = "ERR_ORDER_PARTIAL_CANCEL_02::취소 대상 상품에 중복된 pricePolicyId가 포함되어 있습니다.";
    private static final String NOT_IN_ORDER_MESSAGE
            = "ERR_ORDER_PARTIAL_CANCEL_03::취소 대상에 주문에 포함되지 않은 상품이 있습니다.";
    private static final String ALREADY_CANCELLED_MESSAGE
            = "ERR_ORDER_PARTIAL_CANCEL_04::이미 취소된 상품이 취소 대상에 포함되어 있습니다.";

    private InvalidPartialCancelProductException(String message) {
        super(message);
    }

    public static InvalidPartialCancelProductException duplicatePricePolicy() {
        return new InvalidPartialCancelProductException(DUPLICATE_PRICE_POLICY_MESSAGE);
    }

    public static InvalidPartialCancelProductException notInOrder() {
        return new InvalidPartialCancelProductException(NOT_IN_ORDER_MESSAGE);
    }

    public static InvalidPartialCancelProductException alreadyCancelled() {
        return new InvalidPartialCancelProductException(ALREADY_CANCELLED_MESSAGE);
    }
}
