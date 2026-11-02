package com.personal.marketnote.notification.service.vendorcommunication;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.notification.domain.vendorcommunication.NotificationVendorCommunicationHistory;
import com.personal.marketnote.notification.mapper.NotificationVendorCommunicationHistoryCommandToStateMapper;
import com.personal.marketnote.notification.port.in.command.vendorcommunication.NotificationVendorCommunicationHistoryCommand;
import com.personal.marketnote.notification.port.in.usecase.vendorcommunication.NotificationRecordVendorCommunicationHistoryUseCase;
import com.personal.marketnote.notification.port.out.vendorcommunication.SaveNotificationVendorCommunicationHistoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;
import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, propagation = REQUIRES_NEW)
public class NotificationRecordVendorCommunicationHistoryService
        implements NotificationRecordVendorCommunicationHistoryUseCase {

    private final SaveNotificationVendorCommunicationHistoryPort saveNotificationVendorCommunicationHistoryPort;

    @Override
    public NotificationVendorCommunicationHistory record(NotificationVendorCommunicationHistoryCommand command) {
        return saveNotificationVendorCommunicationHistoryPort.save(
                NotificationVendorCommunicationHistory.from(
                        NotificationVendorCommunicationHistoryCommandToStateMapper.mapToCreateState(command)
                )
        );
    }
}
