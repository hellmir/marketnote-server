package com.personal.marketnote.notification.service.preference;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.notification.domain.preference.NotificationPreference;
import com.personal.marketnote.notification.domain.template.NotificationType;
import com.personal.marketnote.notification.mapper.NotificationPreferenceCommandToStateMapper;
import com.personal.marketnote.notification.port.in.command.InitializeNotificationPreferenceCommand;
import com.personal.marketnote.notification.port.in.usecase.preference.InitializeNotificationPreferenceUseCase;
import com.personal.marketnote.notification.port.out.preference.SaveNotificationPreferencePort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
public class InitializeNotificationPreferenceService implements InitializeNotificationPreferenceUseCase {

    private final SaveNotificationPreferencePort saveNotificationPreferencePort;

    @Override
    @Transactional(isolation = READ_COMMITTED)
    public void initializeNotificationPreference(InitializeNotificationPreferenceCommand command) {
        List<NotificationPreference> preferences = Arrays.stream(NotificationType.values())
                .map(type -> NotificationPreference.from(
                        NotificationPreferenceCommandToStateMapper.mapToState(command.userId(), type)
                ))
                .toList();

        saveNotificationPreferencePort.saveAll(preferences);
    }
}
