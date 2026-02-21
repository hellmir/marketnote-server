package com.personal.marketnote.fulfillment.exception;

import com.personal.marketnote.common.exception.ExternalOperationFailedException;
import com.personal.marketnote.common.utility.FormatValidator;

import java.io.IOException;

public class FasstoAuthDisconnectFailedException extends ExternalOperationFailedException {
    private static final String FASSTO_AUTH_DISCONNECT_FAILED_EXCEPTION_MESSAGE =
            "Fassto authentication disconnect request failed.";

    public FasstoAuthDisconnectFailedException(IOException cause) {
        super(FASSTO_AUTH_DISCONNECT_FAILED_EXCEPTION_MESSAGE, cause);
    }

    public FasstoAuthDisconnectFailedException(String vendorMessage, IOException cause) {
        super(resolveMessage(vendorMessage), cause);
    }

    private static String resolveMessage(String vendorMessage) {
        if (FormatValidator.hasNoValue(vendorMessage)) {
            return FASSTO_AUTH_DISCONNECT_FAILED_EXCEPTION_MESSAGE;
        }
        return vendorMessage;
    }
}
