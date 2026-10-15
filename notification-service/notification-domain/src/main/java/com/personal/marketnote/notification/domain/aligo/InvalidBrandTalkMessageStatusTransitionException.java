package com.personal.marketnote.notification.domain.aligo;

public class InvalidBrandTalkMessageStatusTransitionException extends IllegalStateException {

    public InvalidBrandTalkMessageStatusTransitionException(BrandTalkMessageStatus current, BrandTalkMessageStatus target) {
        super("ERR_BRAND_TALK_MESSAGE_STATUS::브랜드메시지 상태 전이가 불가능합니다. current=" + current + ", target=" + target);
    }
}
