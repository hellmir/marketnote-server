package com.personal.marketnote.notification.domain.aligo;

public class AligoMessageNotFoundException extends RuntimeException {

    public AligoMessageNotFoundException(Long id) {
        super("ERR_ALIGO_MESSAGE_01::알림톡 메시지를 찾을 수 없습니다. id=" + id);
    }
}
