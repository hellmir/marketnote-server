package com.personal.marketnote.fulfillment.exception;

import com.personal.marketnote.common.exception.ExternalOperationFailedException;
import com.personal.marketnote.common.utility.FormatValidator;

import java.io.IOException;

public class RegisterFulfillmentGoodsFailedException extends ExternalOperationFailedException {
    private static final String REGISTER_FULFILLMENT_GOODS_FAILED_EXCEPTION_MESSAGE =
            "Fulfillment goods registration request failed.";

    public RegisterFulfillmentGoodsFailedException(IOException cause) {
        super(REGISTER_FULFILLMENT_GOODS_FAILED_EXCEPTION_MESSAGE, cause);
    }

    public RegisterFulfillmentGoodsFailedException(String vendorMessage, IOException cause) {
        super(resolveMessage(vendorMessage), cause);
    }

    private static String resolveMessage(String vendorMessage) {
        if (FormatValidator.hasNoValue(vendorMessage)) {
            return REGISTER_FULFILLMENT_GOODS_FAILED_EXCEPTION_MESSAGE;
        }
        return vendorMessage;
    }
}
