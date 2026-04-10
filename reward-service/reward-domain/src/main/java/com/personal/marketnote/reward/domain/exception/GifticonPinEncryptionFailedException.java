package com.personal.marketnote.reward.domain.exception;

public class GifticonPinEncryptionFailedException extends RuntimeException {
    private static final String MESSAGE = "PIN 암호화에 실패했습니다.";

    public GifticonPinEncryptionFailedException(Throwable cause) {
        super(MESSAGE, cause);
    }
}
