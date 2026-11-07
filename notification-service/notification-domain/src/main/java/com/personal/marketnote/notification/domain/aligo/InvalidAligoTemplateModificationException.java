package com.personal.marketnote.notification.domain.aligo;

public class InvalidAligoTemplateModificationException extends IllegalStateException {
    private static final String MESSAGE = "ERR_ALIGO_TEMPLATE_03::템플릿을 수정할 수 없는 상태입니다.";

    public InvalidAligoTemplateModificationException() {
        super(MESSAGE);
    }
}
