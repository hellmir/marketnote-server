package com.personal.marketnote.fulfillment.exception;

public class FulfillmentInvalidAccessException extends RuntimeException {
    private static final String FULFILLMENT_INVALID_ACCESS_EXCEPTION_MESSAGE =
            "Fulfillment vendor returned INVALID_ACCESS.";

    public FulfillmentInvalidAccessException() {
        super(FULFILLMENT_INVALID_ACCESS_EXCEPTION_MESSAGE);
    }
}
