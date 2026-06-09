package com.personal.marketnote.notification.port.in.usecase.template;

import com.personal.marketnote.notification.port.in.command.UpdateNotificationTemplateCommand;
import com.personal.marketnote.notification.port.in.result.template.UpdateNotificationTemplateResult;

public interface UpdateNotificationTemplateUseCase {

    UpdateNotificationTemplateResult updateNotificationTemplate(Long id, UpdateNotificationTemplateCommand command);
}
