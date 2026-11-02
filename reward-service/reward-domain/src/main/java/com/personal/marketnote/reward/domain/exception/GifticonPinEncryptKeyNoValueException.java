package com.personal.marketnote.reward.domain.exception;

public class GifticonPinEncryptKeyNoValueException extends RuntimeException {
    private static final String MESSAGE = "AES 키가 설정되지 않았습니다.";

    public GifticonPinEncryptKeyNoValueException() {
        super(MESSAGE);
    }
}
