package com.personal.marketnote.fulfillment.exception;

import com.personal.marketnote.common.exception.ExternalOperationFailedException;
import com.personal.marketnote.common.utility.FormatValidator;

import java.io.IOException;

public class UpdateFasstoSupplierFailedException extends ExternalOperationFailedException {
    private static final String UPDATE_FASSTO_SUPPLIER_FAILED_EXCEPTION_MESSAGE =
            "Fassto supplier update request failed.";

    public UpdateFasstoSupplierFailedException(IOException cause) {
        super(UPDATE_FASSTO_SUPPLIER_FAILED_EXCEPTION_MESSAGE, cause);
    }

    public UpdateFasstoSupplierFailedException(String vendorMessage, IOException cause) {
        super(resolveMessage(vendorMessage), cause);
    }

    private static String resolveMessage(String vendorMessage) {
        if (FormatValidator.hasNoValue(vendorMessage)) {
            return UPDATE_FASSTO_SUPPLIER_FAILED_EXCEPTION_MESSAGE;
        }
        return vendorMessage;
    }
}
