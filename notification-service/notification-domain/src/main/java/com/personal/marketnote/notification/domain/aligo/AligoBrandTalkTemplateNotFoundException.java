package com.personal.marketnote.notification.domain.aligo;

import com.personal.marketnote.common.domain.exception.DomainNotFoundException;

public class AligoBrandTalkTemplateNotFoundException extends DomainNotFoundException {
    private static final String MESSAGE = "브랜드메시지 템플릿을 찾을 수 없습니다.";

    public AligoBrandTalkTemplateNotFoundException() {
        super(MESSAGE);
    }
}
