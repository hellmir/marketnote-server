package com.personal.marketnote.notification.domain.aligo;

import com.personal.marketnote.common.domain.exception.DomainNotFoundException;

public class AligoMessageNotFoundException extends DomainNotFoundException {
    private static final String MESSAGE = "알림톡 메시지를 찾을 수 없습니다.";

    public AligoMessageNotFoundException() {
        super(MESSAGE);
    }
}
