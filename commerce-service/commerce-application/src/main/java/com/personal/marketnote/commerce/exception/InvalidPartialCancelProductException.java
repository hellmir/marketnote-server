package com.personal.marketnote.commerce.exception;

public class InvalidPartialCancelProductException extends IllegalArgumentException {
    public InvalidPartialCancelProductException(String message) {
        super("ERR_ORDER_PARTIAL_CANCEL_02::" + message);
    }
}
