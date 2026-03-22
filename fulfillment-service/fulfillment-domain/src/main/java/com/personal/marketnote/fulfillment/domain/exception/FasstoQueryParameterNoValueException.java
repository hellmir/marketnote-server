package com.personal.marketnote.fulfillment.domain.exception;

public class FasstoQueryParameterNoValueException extends IllegalArgumentException {
    public FasstoQueryParameterNoValueException(String parameterName) {
        super(parameterName + " is required.");
    }

    public FasstoQueryParameterNoValueException(String parameterName, String context) {
        super(parameterName + " is required for " + context + ".");
    }
}
