package com.personal.marketnote.fulfillment.service.vendor;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentDeliveryCarCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFulfillmentDeliveryResult;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.RegisterFulfillmentDeliveryCarUseCase;
import com.personal.marketnote.fulfillment.port.out.vendor.RegisterFulfillmentDeliveryCarPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED)
public class RegisterFulfillmentDeliveryCarService implements RegisterFulfillmentDeliveryCarUseCase {
    private final RegisterFulfillmentDeliveryCarPort registerFulfillmentDeliveryCarPort;

    @Override
    public RegisterFulfillmentDeliveryResult registerDeliveryCar(RegisterFulfillmentDeliveryCarCommand command) {
        return registerFulfillmentDeliveryCarPort.registerDeliveryCar(command);
    }
}
