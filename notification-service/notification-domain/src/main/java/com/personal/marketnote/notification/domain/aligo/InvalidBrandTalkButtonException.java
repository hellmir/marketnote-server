package com.personal.marketnote.notification.domain.aligo;

public class InvalidBrandTalkButtonException extends IllegalArgumentException {

    public InvalidBrandTalkButtonException(String message) {
        super("ERR_BRAND_TALK_03::" + message);
    }
}
