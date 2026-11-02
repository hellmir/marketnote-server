package com.personal.marketnote.notification.domain.aligo;

public class InvalidAligoSenderProfileException extends IllegalArgumentException {

    public InvalidAligoSenderProfileException(String message) {
        super("ERR_ALIGO_PROFILE_02::" + message);
    }
}
