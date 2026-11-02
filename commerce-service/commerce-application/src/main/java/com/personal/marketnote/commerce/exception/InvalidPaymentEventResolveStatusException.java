package com.personal.marketnote.commerce.exception;

public class InvalidPaymentEventResolveStatusException extends IllegalArgumentException {
    public InvalidPaymentEventResolveStatusException(String resolvedStatus) {
        super("유효하지 않은 resolve 상태입니다. resolvedStatus=" + resolvedStatus + " (COMPLETE 또는 FAILED만 허용)");
    }
}
