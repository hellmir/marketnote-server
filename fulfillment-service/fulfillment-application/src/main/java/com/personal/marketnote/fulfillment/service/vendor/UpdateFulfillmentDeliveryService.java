package com.personal.marketnote.fulfillment.service.vendor;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.fulfillment.mapper.FulfillmentDeliveryCommandToRequestMapper;
import com.personal.marketnote.fulfillment.port.in.command.vendor.UpdateFulfillmentDeliveryCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFulfillmentDeliveryResult;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.UpdateFulfillmentDeliveryUseCase;
import com.personal.marketnote.fulfillment.port.out.vendor.UpdateFulfillmentDeliveryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED)
public class UpdateFulfillmentDeliveryService implements UpdateFulfillmentDeliveryUseCase {
    private final UpdateFulfillmentDeliveryPort updateFulfillmentDeliveryPort;

    @Override
    public RegisterFulfillmentDeliveryResult updateDelivery(UpdateFulfillmentDeliveryCommand command) {
        return updateFulfillmentDeliveryPort.updateDelivery(
                FulfillmentDeliveryCommandToRequestMapper.mapToUpdateRequest(command)
        );
    }
}
