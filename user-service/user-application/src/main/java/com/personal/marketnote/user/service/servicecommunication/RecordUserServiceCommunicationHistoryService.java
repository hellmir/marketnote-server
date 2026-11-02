package com.personal.marketnote.user.service.servicecommunication;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.user.domain.servicecommunication.UserServiceCommunicationHistory;
import com.personal.marketnote.user.mapper.UserServiceCommunicationHistoryCommandToStateMapper;
import com.personal.marketnote.user.port.in.command.servicecommunication.UserServiceCommunicationHistoryCommand;
import com.personal.marketnote.user.port.in.usecase.servicecommunication.RecordUserServiceCommunicationHistoryUseCase;
import com.personal.marketnote.user.port.out.servicecommunication.SaveUserServiceCommunicationHistoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;
import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, propagation = REQUIRES_NEW)
public class RecordUserServiceCommunicationHistoryService
        implements RecordUserServiceCommunicationHistoryUseCase {
    private final SaveUserServiceCommunicationHistoryPort saveServiceCommunicationHistoryPort;

    @Override
    public UserServiceCommunicationHistory record(UserServiceCommunicationHistoryCommand command) {
        return saveServiceCommunicationHistoryPort.save(
                UserServiceCommunicationHistory.from(
                        UserServiceCommunicationHistoryCommandToStateMapper.mapToCreateState(command)
                )
        );
    }
}
