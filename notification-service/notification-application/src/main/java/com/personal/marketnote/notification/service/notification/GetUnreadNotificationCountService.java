package com.personal.marketnote.notification.service.notification;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.notification.port.in.result.notification.GetUnreadNotificationCountResult;
import com.personal.marketnote.notification.port.in.usecase.notification.GetUnreadNotificationCountUseCase;
import com.personal.marketnote.notification.port.out.notification.FindNotificationPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
public class GetUnreadNotificationCountService implements GetUnreadNotificationCountUseCase {

    private final FindNotificationPort findNotificationPort;

    @Override
    @Transactional(isolation = READ_COMMITTED, readOnly = true)
    public GetUnreadNotificationCountResult getUnreadCount(Long userId) {
        long unreadCount = findNotificationPort.countUnreadByUserId(userId);
        return new GetUnreadNotificationCountResult(unreadCount);
    }
}
