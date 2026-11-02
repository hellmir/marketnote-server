package com.personal.marketnote.notification.port.in.usecase.notification;

import com.personal.marketnote.notification.port.in.command.GetNotificationHistoryCommand;
import com.personal.marketnote.notification.port.in.result.notification.GetNotificationHistoryResult;

public interface GetNotificationHistoryUseCase {

    GetNotificationHistoryResult getNotificationHistory(GetNotificationHistoryCommand command);
}
