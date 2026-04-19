package com.personal.marketnote.fulfillment.exception;

import com.personal.marketnote.common.exception.ExternalOperationFailedException;
import com.personal.marketnote.common.utility.FormatValidator;

import java.io.IOException;

public class UpdateFulfillmentSupplierFailedException extends ExternalOperationFailedException {
    private static final String UPDATE_FULFILLMENT_SUPPLIER_FAILED_EXCEPTION_MESSAGE =
            "Fulfillment supplier update request failed.";

    public UpdateFulfillmentSupplierFailedException(IOException cause) {
        super(UPDATE_FULFILLMENT_SUPPLIER_FAILED_EXCEPTION_MESSAGE, cause);
    }

    public UpdateFulfillmentSupplierFailedException(String vendorMessage, IOException cause) {
        super(resolveMessage(vendorMessage), cause);
    }

    private static String resolveMessage(String vendorMessage) {
        if (FormatValidator.hasNoValue(vendorMessage)) {
            return UPDATE_FULFILLMENT_SUPPLIER_FAILED_EXCEPTION_MESSAGE;
        }
        return vendorMessage;
    }
}
