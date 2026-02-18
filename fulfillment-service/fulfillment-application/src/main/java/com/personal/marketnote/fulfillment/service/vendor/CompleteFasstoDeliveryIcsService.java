package com.personal.marketnote.fulfillment.service.vendor;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.fulfillment.mapper.FasstoDeliveryCommandToRequestMapper;
import com.personal.marketnote.fulfillment.port.in.command.vendor.CompleteFasstoDeliveryIcsCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.CompleteFasstoDeliveryIcsResult;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.CompleteFasstoDeliveryIcsUseCase;
import com.personal.marketnote.fulfillment.port.out.vendor.CompleteFasstoDeliveryIcsPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true)
public class CompleteFasstoDeliveryIcsService implements CompleteFasstoDeliveryIcsUseCase {
    private final CompleteFasstoDeliveryIcsPort completeFasstoDeliveryIcsPort;

    @Override
    public CompleteFasstoDeliveryIcsResult completeDeliveryIcs(CompleteFasstoDeliveryIcsCommand command) {
        return completeFasstoDeliveryIcsPort.completeDeliveryIcs(
                FasstoDeliveryCommandToRequestMapper.mapToIcsCompletionRequest(command)
        );
    }
}
