package com.personal.marketnote.fulfillment.exception;

import com.personal.marketnote.common.exception.ExternalOperationFailedException;
import com.personal.marketnote.common.utility.FormatValidator;

import java.io.IOException;

public class RegisterFulfillmentWarehousingFailedException extends ExternalOperationFailedException {
    private static final String DEFAULT_MESSAGE = "풀필먼트 상품 입고 요청 중 오류가 발생했습니다.";

    public RegisterFulfillmentWarehousingFailedException(IOException cause) {
        super(DEFAULT_MESSAGE, cause);
    }

    public RegisterFulfillmentWarehousingFailedException(String vendorMessage, IOException cause) {
        super(resolveMessage(vendorMessage), cause);
    }

    private static String resolveMessage(String vendorMessage) {
        if (FormatValidator.hasValue(vendorMessage)) {
            return String.format("풀필먼트 상품 입고 요청 실패: %s", vendorMessage);
        }
        return DEFAULT_MESSAGE;
    }
}
