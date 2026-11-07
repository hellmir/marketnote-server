package com.personal.marketnote.notification.domain.aligo;

import com.personal.marketnote.common.domain.exception.DomainNotFoundException;

public class AligoSenderProfileNotFoundException extends DomainNotFoundException {
    private static final String MESSAGE = "발신프로필을 찾을 수 없습니다.";

    public AligoSenderProfileNotFoundException() {
        super(MESSAGE);
    }
}
