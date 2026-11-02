package com.personal.marketnote.notification.domain.aligo;

public class InvalidAligoMessageStatusTransitionException extends IllegalStateException {

    public InvalidAligoMessageStatusTransitionException(AligoMessageStatus currentStatus, AligoMessageStatus targetStatus) {
        super("ERR_ALIGO_MESSAGE_04::메시지 상태를 " + currentStatus.name() + "에서 " + targetStatus.name() + "으로 전이할 수 없습니다.");
    }
}
