package com.personal.marketnote.fulfillment.service.vendor;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.fulfillment.mapper.FulfillmentDeliveryCommandToRequestMapper;
import com.personal.marketnote.fulfillment.port.in.command.vendor.CompleteFulfillmentDeliveryIcsCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.CompleteFulfillmentDeliveryIcsResult;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.CompleteFulfillmentDeliveryIcsUseCase;
import com.personal.marketnote.fulfillment.port.out.vendor.CompleteFulfillmentDeliveryIcsPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true)
public class CompleteFulfillmentDeliveryIcsService implements CompleteFulfillmentDeliveryIcsUseCase {
    private final CompleteFulfillmentDeliveryIcsPort completeFulfillmentDeliveryIcsPort;

    @Override
    public CompleteFulfillmentDeliveryIcsResult completeDeliveryIcs(CompleteFulfillmentDeliveryIcsCommand command) {
        return completeFulfillmentDeliveryIcsPort.completeDeliveryIcs(
                FulfillmentDeliveryCommandToRequestMapper.mapToIcsCompletionRequest(command)
        );
    }
}
