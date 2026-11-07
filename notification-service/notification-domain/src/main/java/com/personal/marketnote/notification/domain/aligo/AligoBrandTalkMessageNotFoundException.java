package com.personal.marketnote.notification.domain.aligo;

import com.personal.marketnote.common.domain.exception.DomainNotFoundException;

public class AligoBrandTalkMessageNotFoundException extends DomainNotFoundException {
    private static final String MESSAGE = "브랜드메시지를 찾을 수 없습니다.";

    public AligoBrandTalkMessageNotFoundException() {
        super(MESSAGE);
    }
}
