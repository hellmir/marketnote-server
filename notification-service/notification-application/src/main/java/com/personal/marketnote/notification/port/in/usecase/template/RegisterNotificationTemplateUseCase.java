package com.personal.marketnote.notification.port.in.usecase.template;

import com.personal.marketnote.notification.port.in.command.RegisterNotificationTemplateCommand;
import com.personal.marketnote.notification.port.in.result.template.RegisterNotificationTemplateResult;

public interface RegisterNotificationTemplateUseCase {

    RegisterNotificationTemplateResult registerNotificationTemplate(RegisterNotificationTemplateCommand command);
}
