package com.personal.marketnote.fulfillment.exception;

import com.personal.marketnote.common.exception.ExternalOperationFailedException;
import com.personal.marketnote.common.utility.FormatValidator;

import java.io.IOException;

public class GetFulfillmentSuppliersFailedException extends ExternalOperationFailedException {
    private static final String GET_FULFILLMENT_SUPPLIER_LIST_FAILED_EXCEPTION_MESSAGE =
            "Fulfillment supplier list request failed.";

    public GetFulfillmentSuppliersFailedException(IOException cause) {
        super(GET_FULFILLMENT_SUPPLIER_LIST_FAILED_EXCEPTION_MESSAGE, cause);
    }

    public GetFulfillmentSuppliersFailedException(String vendorMessage, IOException cause) {
        super(resolveMessage(vendorMessage), cause);
    }

    private static String resolveMessage(String vendorMessage) {
        if (FormatValidator.hasNoValue(vendorMessage)) {
            return GET_FULFILLMENT_SUPPLIER_LIST_FAILED_EXCEPTION_MESSAGE;
        }
        return vendorMessage;
    }
}
