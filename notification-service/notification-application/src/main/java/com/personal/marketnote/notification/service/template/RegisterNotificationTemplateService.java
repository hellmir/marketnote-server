package com.personal.marketnote.notification.service.template;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.notification.domain.template.DuplicateNotificationTemplateException;
import com.personal.marketnote.notification.domain.template.NotificationTemplate;
import com.personal.marketnote.notification.mapper.NotificationTemplateCommandToStateMapper;
import com.personal.marketnote.notification.port.in.command.RegisterNotificationTemplateCommand;
import com.personal.marketnote.notification.port.in.result.template.RegisterNotificationTemplateResult;
import com.personal.marketnote.notification.port.in.usecase.template.RegisterNotificationTemplateUseCase;
import com.personal.marketnote.notification.port.out.template.FindNotificationTemplatePort;
import com.personal.marketnote.notification.port.out.template.SaveNotificationTemplatePort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
public class RegisterNotificationTemplateService implements RegisterNotificationTemplateUseCase {

    private final FindNotificationTemplatePort findNotificationTemplatePort;
    private final SaveNotificationTemplatePort saveNotificationTemplatePort;

    @Override
    @Transactional(isolation = READ_COMMITTED)
    public RegisterNotificationTemplateResult registerNotificationTemplate(RegisterNotificationTemplateCommand command) {
        validateNoDuplicateTemplateCode(command.templateCode());

        NotificationTemplate template = NotificationTemplate.from(
                NotificationTemplateCommandToStateMapper.mapToState(command));

        Long savedId = saveNotificationTemplatePort.save(template);

        return RegisterNotificationTemplateResult.of(savedId);
    }

    private void validateNoDuplicateTemplateCode(String templateCode) {
        findNotificationTemplatePort.findActiveByTemplateCode(templateCode)
                .ifPresent(existing -> {
                    throw new DuplicateNotificationTemplateException(templateCode);
                });
    }
}
