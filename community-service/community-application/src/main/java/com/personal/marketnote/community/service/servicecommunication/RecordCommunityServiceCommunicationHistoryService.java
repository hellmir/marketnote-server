package com.personal.marketnote.community.service.servicecommunication;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.community.domain.servicecommunication.CommunityServiceCommunicationHistory;
import com.personal.marketnote.community.mapper.CommunityServiceCommunicationHistoryCommandToStateMapper;
import com.personal.marketnote.community.port.in.command.servicecommunication.CommunityServiceCommunicationHistoryCommand;
import com.personal.marketnote.community.port.in.usecase.servicecommunication.RecordCommunityServiceCommunicationHistoryUseCase;
import com.personal.marketnote.community.port.out.servicecommunication.SaveCommunityServiceCommunicationHistoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;
import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, propagation = REQUIRES_NEW)
public class RecordCommunityServiceCommunicationHistoryService
        implements RecordCommunityServiceCommunicationHistoryUseCase {
    private final SaveCommunityServiceCommunicationHistoryPort saveServiceCommunicationHistoryPort;

    @Override
    public CommunityServiceCommunicationHistory record(CommunityServiceCommunicationHistoryCommand command) {
        return saveServiceCommunicationHistoryPort.save(
                CommunityServiceCommunicationHistory.from(
                        CommunityServiceCommunicationHistoryCommandToStateMapper.mapToCreateState(command)
                )
        );
    }
}
