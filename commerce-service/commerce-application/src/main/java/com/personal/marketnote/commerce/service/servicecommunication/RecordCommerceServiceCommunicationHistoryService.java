package com.personal.marketnote.commerce.service.servicecommunication;

import com.personal.marketnote.commerce.domain.servicecommunication.CommerceServiceCommunicationHistory;
import com.personal.marketnote.commerce.mapper.CommerceServiceCommunicationHistoryCommandToStateMapper;
import com.personal.marketnote.commerce.port.in.command.servicecommunication.CommerceServiceCommunicationHistoryCommand;
import com.personal.marketnote.commerce.port.in.usecase.servicecommunication.RecordCommerceServiceCommunicationHistoryUseCase;
import com.personal.marketnote.commerce.port.out.servicecommunication.SaveCommerceServiceCommunicationHistoryPort;
import com.personal.marketnote.common.application.UseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;
import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, propagation = REQUIRES_NEW)
public class RecordCommerceServiceCommunicationHistoryService
        implements RecordCommerceServiceCommunicationHistoryUseCase {
    private final SaveCommerceServiceCommunicationHistoryPort saveServiceCommunicationHistoryPort;

    @Override
    public CommerceServiceCommunicationHistory record(CommerceServiceCommunicationHistoryCommand command) {
        return saveServiceCommunicationHistoryPort.save(
                CommerceServiceCommunicationHistory.from(
                        CommerceServiceCommunicationHistoryCommandToStateMapper.mapToCreateState(command)
                )
        );
    }
}
