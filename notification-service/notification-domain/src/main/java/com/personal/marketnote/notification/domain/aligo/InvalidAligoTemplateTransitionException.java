package com.personal.marketnote.notification.domain.aligo;

public class InvalidAligoTemplateTransitionException extends IllegalStateException {

    public InvalidAligoTemplateTransitionException(String from, String to) {
        super("ERR_ALIGO_TEMPLATE_03::알림톡 템플릿 상태 전이가 허용되지 않습니다. " + from + " → " + to);
    }
}
