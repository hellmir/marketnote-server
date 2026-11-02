package com.personal.marketnote.notification.port.in.usecase.preference;

import com.personal.marketnote.notification.port.in.result.preference.GetNotificationPreferenceResult;

import java.util.List;

public interface GetNotificationPreferenceUseCase {
    List<GetNotificationPreferenceResult> getNotificationPreferences(Long userId);
}
