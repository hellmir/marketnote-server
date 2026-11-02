package com.personal.marketnote.commerce.service.vendorcommunication;

import com.personal.marketnote.commerce.domain.vendorcommunication.CommerceVendorCommunicationHistory;
import com.personal.marketnote.commerce.mapper.CommerceVendorCommunicationHistoryCommandToStateMapper;
import com.personal.marketnote.commerce.port.in.command.vendorcommunication.CommerceVendorCommunicationHistoryCommand;
import com.personal.marketnote.commerce.port.in.usecase.vendorcommunication.RecordCommerceVendorCommunicationHistoryUseCase;
import com.personal.marketnote.commerce.port.out.vendorcommunication.SaveCommerceVendorCommunicationHistoryPort;
import com.personal.marketnote.common.application.UseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;
import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, propagation = REQUIRES_NEW)
public class RecordCommerceVendorCommunicationHistoryService
        implements RecordCommerceVendorCommunicationHistoryUseCase {
    private final SaveCommerceVendorCommunicationHistoryPort saveVendorCommunicationHistoryPort;

    @Override
    public CommerceVendorCommunicationHistory record(CommerceVendorCommunicationHistoryCommand command) {
        return saveVendorCommunicationHistoryPort.save(
                CommerceVendorCommunicationHistory.from(
                        CommerceVendorCommunicationHistoryCommandToStateMapper.mapToCreateState(command)
                )
        );
    }
}
