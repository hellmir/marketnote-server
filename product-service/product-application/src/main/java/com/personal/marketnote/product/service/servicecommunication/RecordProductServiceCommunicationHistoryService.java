package com.personal.marketnote.product.service.servicecommunication;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.product.domain.servicecommunication.ProductServiceCommunicationHistory;
import com.personal.marketnote.product.mapper.ProductServiceCommunicationHistoryCommandToStateMapper;
import com.personal.marketnote.product.port.in.command.servicecommunication.ProductServiceCommunicationHistoryCommand;
import com.personal.marketnote.product.port.in.usecase.servicecommunication.RecordProductServiceCommunicationHistoryUseCase;
import com.personal.marketnote.product.port.out.servicecommunication.SaveProductServiceCommunicationHistoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;
import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, propagation = REQUIRES_NEW)
public class RecordProductServiceCommunicationHistoryService
        implements RecordProductServiceCommunicationHistoryUseCase {
    private final SaveProductServiceCommunicationHistoryPort saveServiceCommunicationHistoryPort;

    @Override
    public ProductServiceCommunicationHistory record(ProductServiceCommunicationHistoryCommand command) {
        return saveServiceCommunicationHistoryPort.save(
                ProductServiceCommunicationHistory.from(
                        ProductServiceCommunicationHistoryCommandToStateMapper.mapToCreateState(command)
                )
        );
    }
}
