package com.personal.marketnote.notification.domain.aligo;

public class InvalidAligoTemplateButtonException extends IllegalArgumentException {

    public InvalidAligoTemplateButtonException(String message) {
        super("ERR_ALIGO_TEMPLATE_05::" + message);
    }
}
