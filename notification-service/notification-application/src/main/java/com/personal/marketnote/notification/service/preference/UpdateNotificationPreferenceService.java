package com.personal.marketnote.notification.service.preference;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.notification.domain.preference.NotificationPreference;
import com.personal.marketnote.notification.domain.preference.NotificationPreferenceNotFoundException;
import com.personal.marketnote.notification.domain.template.NotificationType;
import com.personal.marketnote.notification.port.in.command.UpdateNotificationPreferenceCommand;
import com.personal.marketnote.notification.port.in.usecase.preference.UpdateNotificationPreferenceUseCase;
import com.personal.marketnote.notification.port.out.preference.FindNotificationPreferencePort;
import com.personal.marketnote.notification.port.out.preference.UpdateNotificationPreferencePort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
public class UpdateNotificationPreferenceService implements UpdateNotificationPreferenceUseCase {

    private final FindNotificationPreferencePort findNotificationPreferencePort;
    private final UpdateNotificationPreferencePort updateNotificationPreferencePort;
    private final Clock clock;

    @Override
    @Transactional(isolation = READ_COMMITTED)
    public void updateNotificationPreference(UpdateNotificationPreferenceCommand command) {
        NotificationType notificationType = NotificationType.valueOf(command.notificationType());

        NotificationPreference preference = findNotificationPreferencePort
                .findByUserIdAndNotificationType(command.userId(), notificationType)
                .orElseThrow(() -> new NotificationPreferenceNotFoundException(command.userId(), notificationType));

        LocalDateTime now = LocalDateTime.now(clock);

        if (command.enabled()) {
            preference.enable(now);
        } else {
            preference.disable(now);
        }

        updateNotificationPreferencePort.update(preference);
    }
}
