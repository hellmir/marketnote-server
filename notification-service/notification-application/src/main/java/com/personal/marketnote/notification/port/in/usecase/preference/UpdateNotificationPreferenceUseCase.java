package com.personal.marketnote.notification.port.in.usecase.preference;

import com.personal.marketnote.notification.port.in.command.UpdateNotificationPreferenceCommand;

public interface UpdateNotificationPreferenceUseCase {
    void updateNotificationPreference(UpdateNotificationPreferenceCommand command);
}
