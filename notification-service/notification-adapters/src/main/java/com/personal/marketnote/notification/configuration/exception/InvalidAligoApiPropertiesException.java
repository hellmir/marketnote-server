package com.personal.marketnote.notification.configuration.exception;

public class InvalidAligoApiPropertiesException extends IllegalStateException {

    public InvalidAligoApiPropertiesException(String violation) {
        super("Aligo API 설정 검증 실패: " + violation);
    }

    public InvalidAligoApiPropertiesException(String violation, Throwable cause) {
        super("Aligo API 설정 검증 실패: " + violation, cause);
    }
}
