package com.personal.marketnote.notification.service.template;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.notification.domain.template.NotificationTemplate;
import com.personal.marketnote.notification.domain.template.NotificationTemplateNotFoundException;
import com.personal.marketnote.notification.port.in.result.template.GetNotificationTemplateResult;
import com.personal.marketnote.notification.port.in.usecase.template.GetNotificationTemplateUseCase;
import com.personal.marketnote.notification.port.out.template.FindNotificationTemplatePort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
public class GetNotificationTemplateService implements GetNotificationTemplateUseCase {

    private final FindNotificationTemplatePort findNotificationTemplatePort;

    @Override
    @Transactional(isolation = READ_COMMITTED, readOnly = true)
    public GetNotificationTemplateResult getNotificationTemplate(Long id) {
        NotificationTemplate template = findNotificationTemplate(id);
        return GetNotificationTemplateResult.from(template);
    }

    @Override
    @Transactional(isolation = READ_COMMITTED, readOnly = true)
    public List<GetNotificationTemplateResult> getNotificationTemplates() {
        return findNotificationTemplatePort.findAllActive().stream()
                .map(GetNotificationTemplateResult::from)
                .toList();
    }

    private NotificationTemplate findNotificationTemplate(Long id) {
        return findNotificationTemplatePort.findActiveById(id)
                .orElseThrow(() -> new NotificationTemplateNotFoundException(id));
    }
}
