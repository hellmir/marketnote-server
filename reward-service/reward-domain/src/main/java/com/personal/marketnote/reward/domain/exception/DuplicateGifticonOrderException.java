package com.personal.marketnote.reward.domain.exception;

public class DuplicateGifticonOrderException extends RuntimeException {
    private static final String MESSAGE = "기프티콘 주문이 중복되었습니다. trId=%s";

    public DuplicateGifticonOrderException(String trId) {
        super(String.format(MESSAGE, trId));
    }
}
