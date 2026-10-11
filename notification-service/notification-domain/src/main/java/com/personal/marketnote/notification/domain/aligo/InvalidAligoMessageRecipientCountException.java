package com.personal.marketnote.notification.domain.aligo;

public class InvalidAligoMessageRecipientCountException extends IllegalArgumentException {

    public InvalidAligoMessageRecipientCountException(int count) {
        super("ERR_ALIGO_MESSAGE_03::수신자 수는 1명 이상 500명 이하여야 합니다. count=" + count);
    }
}
