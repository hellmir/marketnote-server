package com.personal.marketnote.reward.domain.exception;

public class InvalidGifticonOrderStatusFilterException extends IllegalArgumentException {
    private static final String MESSAGE = "유효하지 않은 기프티콘 주문 상태 필터입니다. value=%s";

    public InvalidGifticonOrderStatusFilterException(String value) {
        super(String.format(MESSAGE, value));
    }
}
