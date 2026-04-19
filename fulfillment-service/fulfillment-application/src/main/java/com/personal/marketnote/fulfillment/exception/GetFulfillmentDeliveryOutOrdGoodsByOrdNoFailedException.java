package com.personal.marketnote.fulfillment.exception;

import com.personal.marketnote.common.exception.ExternalOperationFailedException;
import com.personal.marketnote.common.utility.FormatValidator;

import java.io.IOException;

public class GetFulfillmentDeliveryOutOrdGoodsByOrdNoFailedException extends ExternalOperationFailedException {
    private static final String DEFAULT_MESSAGE = "풀필먼트 주문번호 기반 출고중 상품 조회 중 오류가 발생했습니다.";

    public GetFulfillmentDeliveryOutOrdGoodsByOrdNoFailedException(IOException cause) {
        super(DEFAULT_MESSAGE, cause);
    }

    public GetFulfillmentDeliveryOutOrdGoodsByOrdNoFailedException(String vendorMessage, IOException cause) {
        super(resolveMessage(vendorMessage), cause);
    }

    private static String resolveMessage(String vendorMessage) {
        if (FormatValidator.hasValue(vendorMessage)) {
            return String.format("풀필먼트 주문번호 기반 출고중 상품 조회 실패: %s", vendorMessage);
        }
        return DEFAULT_MESSAGE;
    }
}
