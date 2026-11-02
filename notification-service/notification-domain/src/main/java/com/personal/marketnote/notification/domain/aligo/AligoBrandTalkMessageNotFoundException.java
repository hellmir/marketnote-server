package com.personal.marketnote.notification.domain.aligo;

public class AligoBrandTalkMessageNotFoundException extends RuntimeException {

    public AligoBrandTalkMessageNotFoundException(Long id) {
        super("ERR_BRAND_TALK_MESSAGE_NOT_FOUND::브랜드메시지를 찾을 수 없습니다. id=" + id);
    }
}
