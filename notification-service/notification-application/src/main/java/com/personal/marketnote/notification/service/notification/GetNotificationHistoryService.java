package com.personal.marketnote.notification.service.notification;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.notification.domain.notification.Notification;
import com.personal.marketnote.notification.port.in.command.GetNotificationHistoryCommand;
import com.personal.marketnote.notification.port.in.result.notification.GetNotificationHistoryResult;
import com.personal.marketnote.notification.port.in.usecase.notification.GetNotificationHistoryUseCase;
import com.personal.marketnote.notification.port.out.notification.FindNotificationPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true)
public class GetNotificationHistoryService implements GetNotificationHistoryUseCase {

    private final FindNotificationPort findNotificationPort;

    @Override
    public GetNotificationHistoryResult getNotificationHistory(GetNotificationHistoryCommand command) {
        int fetchSize = command.pageSize() + 1;
        List<Notification> notifications = findNotificationPort.findByUserId(
                command.userId(), command.cursor(), fetchSize
        );

        boolean hasNext = notifications.size() > command.pageSize();
        List<Notification> pagedNotifications = hasNext
                ? notifications.subList(0, command.pageSize())
                : notifications;

        Long nextCursor = pagedNotifications.isEmpty() ? null : pagedNotifications.getLast().getId();

        Long totalElements = resolveTotalElements(command, hasNext, pagedNotifications.size());

        return GetNotificationHistoryResult.from(totalElements, hasNext, nextCursor, pagedNotifications);
    }

    private Long resolveTotalElements(GetNotificationHistoryCommand command,
                                      boolean hasNext, int currentSize) {
        if (FormatValidator.hasValue(command.cursor())) {
            return null;
        }
        if (!hasNext) {
            return (long) currentSize;
        }
        return findNotificationPort.countByUserId(command.userId());
    }
}
