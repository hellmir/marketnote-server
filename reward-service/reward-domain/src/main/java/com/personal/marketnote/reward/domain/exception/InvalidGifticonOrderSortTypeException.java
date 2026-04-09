package com.personal.marketnote.reward.domain.exception;

public class InvalidGifticonOrderSortTypeException extends IllegalArgumentException {
    private static final String MESSAGE = "유효하지 않은 기프티콘 주문 정렬 타입입니다. value=%s";

    public InvalidGifticonOrderSortTypeException(String value) {
        super(String.format(MESSAGE, value));
    }
}
