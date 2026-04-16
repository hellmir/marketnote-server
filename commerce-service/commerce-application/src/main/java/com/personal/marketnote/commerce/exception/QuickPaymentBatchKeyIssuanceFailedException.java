package com.personal.marketnote.commerce.exception;

public class QuickPaymentBatchKeyIssuanceFailedException extends RuntimeException {
    private static final String MESSAGE = "빠른결제 배치키 발급 실패 [%s]: %s";

    public QuickPaymentBatchKeyIssuanceFailedException(String resultCode, String resultMessage) {
        super(String.format(MESSAGE, resultCode, resultMessage));
    }
}
