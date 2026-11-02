package com.personal.marketnote.notification.domain.aligo;

public class InvalidBrandTalkTemplateException extends IllegalArgumentException {

    public InvalidBrandTalkTemplateException(String message) {
        super("ERR_BRAND_TALK_TEMPLATE_02::" + message);
    }
}
