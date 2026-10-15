package com.personal.marketnote.notification.domain.aligo;

public class InvalidBrandTalkMessageException extends IllegalArgumentException {

    public InvalidBrandTalkMessageException(String message) {
        super("ERR_BRAND_TALK_MESSAGE::유효하지 않은 브랜드메시지입니다. " + message);
    }
}
