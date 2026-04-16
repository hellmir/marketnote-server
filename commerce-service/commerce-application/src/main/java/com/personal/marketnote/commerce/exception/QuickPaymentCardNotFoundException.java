package com.personal.marketnote.commerce.exception;

public class QuickPaymentCardNotFoundException extends RuntimeException {
    private static final String MESSAGE = "빠른결제 카드를 찾을 수 없습니다. id=%d, userId=%d";

    public QuickPaymentCardNotFoundException(Long id, Long userId) {
        super(String.format(MESSAGE, id, userId));
    }
}
