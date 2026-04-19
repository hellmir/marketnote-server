package com.personal.marketnote.fulfillment.exception;

import com.personal.marketnote.common.exception.ExternalOperationFailedException;
import com.personal.marketnote.common.utility.FormatValidator;

import java.io.IOException;

public class GetFulfillmentWarehousingAbnormalImageFailedException extends ExternalOperationFailedException {
    private static final String DEFAULT_MESSAGE = "풀필먼트 비정상 입고 상품 이미지 조회 중 오류가 발생했습니다.";

    public GetFulfillmentWarehousingAbnormalImageFailedException(IOException cause) {
        super(DEFAULT_MESSAGE, cause);
    }

    public GetFulfillmentWarehousingAbnormalImageFailedException(String vendorMessage, IOException cause) {
        super(resolveMessage(vendorMessage), cause);
    }

    private static String resolveMessage(String vendorMessage) {
        if (FormatValidator.hasValue(vendorMessage)) {
            return String.format("풀필먼트 비정상 입고 상품 이미지 조회 실패: %s", vendorMessage);
        }
        return DEFAULT_MESSAGE;
    }
}
