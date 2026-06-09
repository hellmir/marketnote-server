package com.personal.marketnote.notification.domain.template;

public class InvalidTemplateCodeException extends IllegalArgumentException {
    public InvalidTemplateCodeException(String message) {
        super("ERR_NOTIFICATION_TEMPLATE_01::" + message);
    }
}
