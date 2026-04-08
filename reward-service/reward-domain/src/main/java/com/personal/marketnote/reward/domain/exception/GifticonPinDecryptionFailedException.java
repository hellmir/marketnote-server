package com.personal.marketnote.reward.domain.exception;

public class GifticonPinDecryptionFailedException extends RuntimeException {
    private static final String MESSAGE = "기프티콘 PIN 복호화에 실패했습니다.";

    public GifticonPinDecryptionFailedException(Throwable cause) {
        super(MESSAGE, cause);
    }
}
