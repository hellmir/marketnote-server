package com.personal.marketnote.fulfillment.exception;

import com.personal.marketnote.common.exception.ExternalOperationFailedException;
import com.personal.marketnote.common.utility.FormatValidator;

import java.io.IOException;

public class RegisterFasstoGoodsFailedException extends ExternalOperationFailedException {
    private static final String REGISTER_FASSTO_GOODS_FAILED_EXCEPTION_MESSAGE =
            "Fassto goods registration request failed.";

    public RegisterFasstoGoodsFailedException(IOException cause) {
        super(REGISTER_FASSTO_GOODS_FAILED_EXCEPTION_MESSAGE, cause);
    }

    public RegisterFasstoGoodsFailedException(String vendorMessage, IOException cause) {
        super(resolveMessage(vendorMessage), cause);
    }

    private static String resolveMessage(String vendorMessage) {
        if (FormatValidator.hasNoValue(vendorMessage)) {
            return REGISTER_FASSTO_GOODS_FAILED_EXCEPTION_MESSAGE;
        }
        return vendorMessage;
    }
}
