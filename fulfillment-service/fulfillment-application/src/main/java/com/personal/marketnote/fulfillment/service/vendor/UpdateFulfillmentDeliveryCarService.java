package com.personal.marketnote.fulfillment.service.vendor;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.fulfillment.port.in.command.vendor.UpdateFulfillmentDeliveryCarCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFulfillmentDeliveryResult;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.UpdateFulfillmentDeliveryCarUseCase;
import com.personal.marketnote.fulfillment.port.out.vendor.UpdateFulfillmentDeliveryCarPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED)
public class UpdateFulfillmentDeliveryCarService implements UpdateFulfillmentDeliveryCarUseCase {
    private final UpdateFulfillmentDeliveryCarPort updateFulfillmentDeliveryCarPort;

    @Override
    public RegisterFulfillmentDeliveryResult updateDeliveryCar(UpdateFulfillmentDeliveryCarCommand command) {
        return updateFulfillmentDeliveryCarPort.updateDeliveryCar(command);
    }
}
