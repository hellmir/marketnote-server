package com.personal.marketnote.notification.port.in.usecase.notification;

import com.personal.marketnote.notification.port.in.result.notification.GetUnreadNotificationCountResult;

public interface GetUnreadNotificationCountUseCase {
    GetUnreadNotificationCountResult getUnreadCount(Long userId);
}
