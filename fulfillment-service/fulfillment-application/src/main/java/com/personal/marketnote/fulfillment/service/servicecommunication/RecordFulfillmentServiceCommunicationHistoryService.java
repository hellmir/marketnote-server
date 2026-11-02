package com.personal.marketnote.fulfillment.service.servicecommunication;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.fulfillment.domain.servicecommunication.FulfillmentServiceCommunicationHistory;
import com.personal.marketnote.fulfillment.mapper.FulfillmentServiceCommunicationHistoryCommandToStateMapper;
import com.personal.marketnote.fulfillment.port.in.command.servicecommunication.FulfillmentServiceCommunicationHistoryCommand;
import com.personal.marketnote.fulfillment.port.in.usecase.servicecommunication.RecordFulfillmentServiceCommunicationHistoryUseCase;
import com.personal.marketnote.fulfillment.port.out.servicecommunication.SaveFulfillmentServiceCommunicationHistoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;
import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, propagation = REQUIRES_NEW)
public class RecordFulfillmentServiceCommunicationHistoryService
        implements RecordFulfillmentServiceCommunicationHistoryUseCase {
    private final SaveFulfillmentServiceCommunicationHistoryPort saveServiceCommunicationHistoryPort;

    @Override
    public FulfillmentServiceCommunicationHistory record(FulfillmentServiceCommunicationHistoryCommand command) {
        return saveServiceCommunicationHistoryPort.save(
                FulfillmentServiceCommunicationHistory.from(
                        FulfillmentServiceCommunicationHistoryCommandToStateMapper.mapToCreateState(command)
                )
        );
    }
}
