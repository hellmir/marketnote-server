package com.personal.marketnote.notification.domain.aligo;

import com.personal.marketnote.common.domain.exception.DomainNotFoundException;

public class AligoTemplateNotFoundException extends DomainNotFoundException {
    private static final String MESSAGE = "알림톡 템플릿을 찾을 수 없습니다.";

    public AligoTemplateNotFoundException() {
        super(MESSAGE);
    }
}
