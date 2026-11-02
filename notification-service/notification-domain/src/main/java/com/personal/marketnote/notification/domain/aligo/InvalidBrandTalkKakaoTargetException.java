package com.personal.marketnote.notification.domain.aligo;

public class InvalidBrandTalkKakaoTargetException extends IllegalArgumentException {

    public InvalidBrandTalkKakaoTargetException(String code) {
        super("ERR_BRAND_TALK_KAKAO_TARGET::알 수 없는 카카오 타겟입니다. code=" + code);
    }
}
