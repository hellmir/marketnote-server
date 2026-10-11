package com.personal.marketnote.notification.domain.aligo;

public class InvalidAligoTemplateException extends IllegalArgumentException {

    public InvalidAligoTemplateException(String message) {
        super("ERR_ALIGO_TEMPLATE_02::" + message);
    }
}
