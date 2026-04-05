package com.personal.marketnote.reward.domain.exception;

public class GifticonOrderNotFoundException extends RuntimeException {
    private static final String MESSAGE = "기프티콘 주문을 찾을 수 없습니다. trId=%s";

    public GifticonOrderNotFoundException(String trId) {
        super(String.format(MESSAGE, trId));
    }
}
