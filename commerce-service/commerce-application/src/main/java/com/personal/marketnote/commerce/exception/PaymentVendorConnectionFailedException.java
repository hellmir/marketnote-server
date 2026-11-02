package com.personal.marketnote.commerce.exception;

public class PaymentVendorConnectionFailedException extends RuntimeException {
    public PaymentVendorConnectionFailedException(String message) {
        super(message);
    }

    public PaymentVendorConnectionFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
