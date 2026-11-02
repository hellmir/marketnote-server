package com.personal.marketnote.fulfillment.domain.exception;

public class FulfillmentQueryParameterNoValueException extends IllegalArgumentException {
    public FulfillmentQueryParameterNoValueException(String parameterName) {
        super(parameterName + " is required.");
    }

    public FulfillmentQueryParameterNoValueException(String parameterName, String context) {
        super(parameterName + " is required for " + context + ".");
    }
}
