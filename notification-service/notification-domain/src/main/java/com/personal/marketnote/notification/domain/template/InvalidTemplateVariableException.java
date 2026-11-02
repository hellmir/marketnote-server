package com.personal.marketnote.notification.domain.template;

public class InvalidTemplateVariableException extends IllegalArgumentException {
    public InvalidTemplateVariableException(String unresolvedVariables) {
        super("ERR_NOTIFICATION_TEMPLATE_02::미치환 변수가 존재합니다. variables=" + unresolvedVariables);
    }
}
