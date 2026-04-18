package com.personal.marketnote.fulfillment.service.vendor;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentDeliveryIcsCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFulfillmentDeliveryResult;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.RegisterFulfillmentDeliveryIcsUseCase;
import com.personal.marketnote.fulfillment.port.out.vendor.RegisterFulfillmentDeliveryIcsPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED)
public class RegisterFulfillmentDeliveryIcsService implements RegisterFulfillmentDeliveryIcsUseCase {
    private final RegisterFulfillmentDeliveryIcsPort registerFulfillmentDeliveryIcsPort;

    @Override
    public RegisterFulfillmentDeliveryResult registerDeliveryIcs(RegisterFulfillmentDeliveryIcsCommand command) {
        return registerFulfillmentDeliveryIcsPort.registerDeliveryIcs(command);
    }
}
