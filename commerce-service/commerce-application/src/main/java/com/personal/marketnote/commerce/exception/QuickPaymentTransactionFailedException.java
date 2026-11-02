package com.personal.marketnote.commerce.exception;

public class QuickPaymentTransactionFailedException extends RuntimeException {
    private static final String MESSAGE = "빠른결제 거래등록 실패 [%s]: %s";

    public QuickPaymentTransactionFailedException(String resultCode, String resultMessage) {
        super(String.format(MESSAGE, resultCode, resultMessage));
    }
}
