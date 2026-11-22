package com.personal.marketnote.fulfillment.configuration.exception;

public class InvalidFulfillmentAuthPropertiesException extends IllegalStateException {
    public InvalidFulfillmentAuthPropertiesException(String violation) {
        super("Fulfillment Auth 설정 검증 실패: " + violation);
    }

    public InvalidFulfillmentAuthPropertiesException(String violation, Throwable cause) {
        super("Fulfillment Auth 설정 검증 실패: " + violation, cause);
    }
}
