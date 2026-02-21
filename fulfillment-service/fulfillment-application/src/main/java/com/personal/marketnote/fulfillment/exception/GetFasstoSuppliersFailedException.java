package com.personal.marketnote.fulfillment.exception;

import com.personal.marketnote.common.exception.ExternalOperationFailedException;
import com.personal.marketnote.common.utility.FormatValidator;

import java.io.IOException;

public class GetFasstoSuppliersFailedException extends ExternalOperationFailedException {
    private static final String GET_FASSTO_SUPPLIER_LIST_FAILED_EXCEPTION_MESSAGE =
            "Fassto supplier list request failed.";

    public GetFasstoSuppliersFailedException(IOException cause) {
        super(GET_FASSTO_SUPPLIER_LIST_FAILED_EXCEPTION_MESSAGE, cause);
    }

    public GetFasstoSuppliersFailedException(String vendorMessage, IOException cause) {
        super(resolveMessage(vendorMessage), cause);
    }

    private static String resolveMessage(String vendorMessage) {
        if (FormatValidator.hasNoValue(vendorMessage)) {
            return GET_FASSTO_SUPPLIER_LIST_FAILED_EXCEPTION_MESSAGE;
        }
        return vendorMessage;
    }
}
