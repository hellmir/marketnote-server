package com.personal.marketnote.commerce.exception;

public class QuickPaymentBatchKeyDeletionFailedException extends RuntimeException {
    private static final String MESSAGE = "빠른결제 배치키 삭제 실패 [%s]: %s";

    public QuickPaymentBatchKeyDeletionFailedException(String resultCode, String resultMessage) {
        super(String.format(MESSAGE, resultCode, resultMessage));
    }
}
