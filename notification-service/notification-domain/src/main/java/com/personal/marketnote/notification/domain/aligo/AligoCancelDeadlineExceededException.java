package com.personal.marketnote.notification.domain.aligo;

public class AligoCancelDeadlineExceededException extends AligoApiException {

    public AligoCancelDeadlineExceededException() {
        super("ERR_ALIGO_04::알리고 예약 취소 기한이 초과되었습니다.");
    }
}
