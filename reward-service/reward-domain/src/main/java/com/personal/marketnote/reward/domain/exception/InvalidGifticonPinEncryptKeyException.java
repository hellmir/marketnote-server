package com.personal.marketnote.reward.domain.exception;

public class InvalidGifticonPinEncryptKeyException extends IllegalArgumentException {
    public InvalidGifticonPinEncryptKeyException(int actualLength) {
        super("AES 키 길이는 16/24/32바이트만 허용합니다. actualLength=" + actualLength);
    }
}
