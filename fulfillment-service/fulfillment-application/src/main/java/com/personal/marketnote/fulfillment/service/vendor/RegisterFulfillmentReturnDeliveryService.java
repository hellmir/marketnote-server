package com.personal.marketnote.fulfillment.service.vendor;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentReturnDeliveryCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFulfillmentDeliveryResult;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.RegisterFulfillmentReturnDeliveryUseCase;
import com.personal.marketnote.fulfillment.port.out.vendor.RegisterFulfillmentReturnDeliveryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true)
public class RegisterFulfillmentReturnDeliveryService implements RegisterFulfillmentReturnDeliveryUseCase {
    private final RegisterFulfillmentReturnDeliveryPort registerFulfillmentReturnDeliveryPort;

    @Override
    public RegisterFulfillmentDeliveryResult registerReturnDelivery(RegisterFulfillmentReturnDeliveryCommand command) {
        return registerFulfillmentReturnDeliveryPort.registerReturnDelivery(command);
    }
}
