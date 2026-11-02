package com.personal.marketnote.notification.port.in.usecase.template;

import com.personal.marketnote.notification.port.in.result.template.GetNotificationTemplateResult;

import java.util.List;

public interface GetNotificationTemplateUseCase {

    GetNotificationTemplateResult getNotificationTemplate(Long id);

    List<GetNotificationTemplateResult> getNotificationTemplates();
}
