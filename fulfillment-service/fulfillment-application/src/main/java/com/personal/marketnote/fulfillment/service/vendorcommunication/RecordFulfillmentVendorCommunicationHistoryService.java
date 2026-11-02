package com.personal.marketnote.fulfillment.service.vendorcommunication;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.fulfillment.domain.vendorcommunication.FulfillmentVendorCommunicationHistory;
import com.personal.marketnote.fulfillment.mapper.FulfillmentVendorCommunicationHistoryCommandToStateMapper;
import com.personal.marketnote.fulfillment.port.in.command.vendorcommunication.FulfillmentVendorCommunicationHistoryCommand;
import com.personal.marketnote.fulfillment.port.in.usecase.vendorcommunication.RecordFulfillmentVendorCommunicationHistoryUseCase;
import com.personal.marketnote.fulfillment.port.out.vendorcommunication.SaveFulfillmentVendorCommunicationHistoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;
import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, propagation = REQUIRES_NEW)
public class RecordFulfillmentVendorCommunicationHistoryService
        implements RecordFulfillmentVendorCommunicationHistoryUseCase {
    private final SaveFulfillmentVendorCommunicationHistoryPort saveVendorCommunicationHistoryPort;

    @Override
    public FulfillmentVendorCommunicationHistory record(FulfillmentVendorCommunicationHistoryCommand command) {
        return saveVendorCommunicationHistoryPort.save(
                FulfillmentVendorCommunicationHistory.from(
                        FulfillmentVendorCommunicationHistoryCommandToStateMapper.mapToCreateState(command)
                )
        );
    }
}
