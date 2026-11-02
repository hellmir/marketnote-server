package com.personal.marketnote.notification.port.in.result.template;

public record RegisterNotificationTemplateResult(Long id) {

    public static RegisterNotificationTemplateResult of(Long id) {
        return new RegisterNotificationTemplateResult(id);
    }
}
