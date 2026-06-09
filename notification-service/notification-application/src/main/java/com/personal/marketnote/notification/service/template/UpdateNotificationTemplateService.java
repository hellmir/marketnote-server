package com.personal.marketnote.notification.service.template;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.notification.domain.template.NotificationTemplate;
import com.personal.marketnote.notification.domain.template.NotificationTemplateNotFoundException;
import com.personal.marketnote.notification.port.in.command.UpdateNotificationTemplateCommand;
import com.personal.marketnote.notification.port.in.result.template.UpdateNotificationTemplateResult;
import com.personal.marketnote.notification.port.in.usecase.template.UpdateNotificationTemplateUseCase;
import com.personal.marketnote.notification.port.out.template.FindNotificationTemplatePort;
import com.personal.marketnote.notification.port.out.template.UpdateNotificationTemplatePort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
public class UpdateNotificationTemplateService implements UpdateNotificationTemplateUseCase {

    private final FindNotificationTemplatePort findNotificationTemplatePort;
    private final UpdateNotificationTemplatePort updateNotificationTemplatePort;

    @Override
    @Transactional(isolation = READ_COMMITTED)
    public UpdateNotificationTemplateResult updateNotificationTemplate(Long id, UpdateNotificationTemplateCommand command) {
        NotificationTemplate template = findNotificationTemplate(id);

        template.update(command.title(), command.bodyTemplate(), command.urlTemplate());

        updateNotificationTemplatePort.update(template);

        return UpdateNotificationTemplateResult.from(template);
    }

    private NotificationTemplate findNotificationTemplate(Long id) {
        return findNotificationTemplatePort.findActiveById(id)
                .orElseThrow(() -> new NotificationTemplateNotFoundException(id));
    }
}
