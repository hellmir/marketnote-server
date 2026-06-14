package com.personal.marketnote.notification.port.in.usecase.preference;

import com.personal.marketnote.notification.port.in.command.InitializeNotificationPreferenceCommand;

public interface InitializeNotificationPreferenceUseCase {
    void initializeNotificationPreference(InitializeNotificationPreferenceCommand command);
}
