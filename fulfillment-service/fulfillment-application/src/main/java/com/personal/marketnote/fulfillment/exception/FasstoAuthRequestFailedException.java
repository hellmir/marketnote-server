package com.personal.marketnote.fulfillment.exception;

import com.personal.marketnote.common.exception.ExternalOperationFailedException;
import com.personal.marketnote.common.utility.FormatValidator;

import java.io.IOException;

public class FasstoAuthRequestFailedException extends ExternalOperationFailedException {
    private static final String FASSTO_AUTH_REQUEST_FAILED_EXCEPTION_MESSAGE =
            "Fassto authentication request failed.";

    public FasstoAuthRequestFailedException(IOException cause) {
        super(FASSTO_AUTH_REQUEST_FAILED_EXCEPTION_MESSAGE, cause);
    }

    public FasstoAuthRequestFailedException(String vendorMessage, IOException cause) {
        super(resolveMessage(vendorMessage), cause);
    }

    private static String resolveMessage(String vendorMessage) {
        if (FormatValidator.hasNoValue(vendorMessage)) {
            return FASSTO_AUTH_REQUEST_FAILED_EXCEPTION_MESSAGE;
        }
        return vendorMessage;
    }
}
