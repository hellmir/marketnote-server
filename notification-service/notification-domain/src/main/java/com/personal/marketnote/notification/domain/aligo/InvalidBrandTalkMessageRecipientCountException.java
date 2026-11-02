package com.personal.marketnote.notification.domain.aligo;

public class InvalidBrandTalkMessageRecipientCountException extends IllegalArgumentException {

    public InvalidBrandTalkMessageRecipientCountException(int count) {
        super("ERR_BRAND_TALK_MESSAGE_RECIPIENT::브랜드메시지 수신자는 1~500명이어야 합니다. count=" + count);
    }
}
