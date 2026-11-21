package com.personal.marketnote.reward.configuration.exception;

public class InvalidGiftishowApiPropertiesException extends IllegalStateException {

    public InvalidGiftishowApiPropertiesException(String violation) {
        super("Giftishow API 설정 검증 실패: " + violation);
    }
}
