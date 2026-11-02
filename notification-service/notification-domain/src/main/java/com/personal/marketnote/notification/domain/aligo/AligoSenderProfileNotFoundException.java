package com.personal.marketnote.notification.domain.aligo;

public class AligoSenderProfileNotFoundException extends RuntimeException {

    public AligoSenderProfileNotFoundException(String senderKey) {
        super("ERR_ALIGO_PROFILE_01::발신프로필을 찾을 수 없습니다. senderKey=" + senderKey);
    }

    public AligoSenderProfileNotFoundException(Long id) {
        super("ERR_ALIGO_PROFILE_01::발신프로필을 찾을 수 없습니다. id=" + id);
    }
}
