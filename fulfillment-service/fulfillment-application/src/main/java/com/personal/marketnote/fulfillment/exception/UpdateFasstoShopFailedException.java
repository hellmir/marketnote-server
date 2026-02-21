package com.personal.marketnote.fulfillment.exception;

import com.personal.marketnote.common.exception.ExternalOperationFailedException;
import com.personal.marketnote.common.utility.FormatValidator;

import java.io.IOException;

public class UpdateFasstoShopFailedException extends ExternalOperationFailedException {
    private static final String UPDATE_FASSTO_MARKETNOTE_FAILED_EXCEPTION_MESSAGE =
            "Fassto shop update request failed.";

    public UpdateFasstoShopFailedException(IOException cause) {
        super(UPDATE_FASSTO_MARKETNOTE_FAILED_EXCEPTION_MESSAGE, cause);
    }

    public UpdateFasstoShopFailedException(String vendorMessage, IOException cause) {
        super(resolveMessage(vendorMessage), cause);
    }

    private static String resolveMessage(String vendorMessage) {
        if (FormatValidator.hasNoValue(vendorMessage)) {
            return UPDATE_FASSTO_MARKETNOTE_FAILED_EXCEPTION_MESSAGE;
        }
        return vendorMessage;
    }
}
