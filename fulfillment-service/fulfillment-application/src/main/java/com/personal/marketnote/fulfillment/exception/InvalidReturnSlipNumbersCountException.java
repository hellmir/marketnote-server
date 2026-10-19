package com.personal.marketnote.fulfillment.exception;

public class InvalidReturnSlipNumbersCountException extends IllegalArgumentException {

    public InvalidReturnSlipNumbersCountException(long count, int maxCount) {
        super("returnSlipNumbers 개수가 최대 제한(" + maxCount + ")을 초과합니다: " + count);
    }
}
