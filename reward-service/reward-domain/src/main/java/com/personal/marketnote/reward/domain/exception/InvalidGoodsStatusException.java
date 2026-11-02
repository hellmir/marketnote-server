package com.personal.marketnote.reward.domain.exception;

public class InvalidGoodsStatusException extends IllegalArgumentException {
    private static final String MESSAGE = "유효하지 않은 기프티콘 상품 상태입니다. value=%s";

    public InvalidGoodsStatusException(String value) {
        super(String.format(MESSAGE, value));
    }
}
