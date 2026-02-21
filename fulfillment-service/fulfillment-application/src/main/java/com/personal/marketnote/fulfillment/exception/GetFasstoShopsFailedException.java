package com.personal.marketnote.fulfillment.exception;

import com.personal.marketnote.common.exception.ExternalOperationFailedException;
import com.personal.marketnote.common.utility.FormatValidator;

import java.io.IOException;

public class GetFasstoShopsFailedException extends ExternalOperationFailedException {
    private static final String GET_FASSTO_MARKETNOTE_LIST_FAILED_EXCEPTION_MESSAGE =
            "Fassto shop list request failed.";

    public GetFasstoShopsFailedException(IOException cause) {
        super(GET_FASSTO_MARKETNOTE_LIST_FAILED_EXCEPTION_MESSAGE, cause);
    }

    public GetFasstoShopsFailedException(String vendorMessage, IOException cause) {
        super(resolveMessage(vendorMessage), cause);
    }

    private static String resolveMessage(String vendorMessage) {
        if (FormatValidator.hasNoValue(vendorMessage)) {
            return GET_FASSTO_MARKETNOTE_LIST_FAILED_EXCEPTION_MESSAGE;
        }
        return vendorMessage;
    }
}
