package com.personal.marketnote.notification.service.template;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.notification.domain.template.NotificationTemplate;
import com.personal.marketnote.notification.domain.template.NotificationTemplateNotFoundException;
import com.personal.marketnote.notification.port.in.usecase.template.DeleteNotificationTemplateUseCase;
import com.personal.marketnote.notification.port.out.template.FindNotificationTemplatePort;
import com.personal.marketnote.notification.port.out.template.UpdateNotificationTemplatePort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
public class DeleteNotificationTemplateService implements DeleteNotificationTemplateUseCase {

    private final FindNotificationTemplatePort findNotificationTemplatePort;
    private final UpdateNotificationTemplatePort updateNotificationTemplatePort;

    @Override
    @Transactional(isolation = READ_COMMITTED)
    public void deleteNotificationTemplate(Long id) {
        NotificationTemplate template = findNotificationTemplate(id);

        template.deactivate();

        updateNotificationTemplatePort.update(template);
    }

    private NotificationTemplate findNotificationTemplate(Long id) {
        return findNotificationTemplatePort.findActiveById(id)
                .orElseThrow(() -> new NotificationTemplateNotFoundException(id));
    }
}
