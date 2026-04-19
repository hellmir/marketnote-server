package com.personal.marketnote.fulfillment.service.vendor;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.fulfillment.domain.delivery.FulfillmentDeliveryRegistration;
import com.personal.marketnote.fulfillment.mapper.FulfillmentCommandToStateMapper;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentDeliveryCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFulfillmentDeliveryResult;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.RegisterFulfillmentDeliveryUseCase;
import com.personal.marketnote.fulfillment.port.out.delivery.SaveFulfillmentDeliveryRegistrationPort;
import com.personal.marketnote.fulfillment.port.out.vendor.RegisterFulfillmentDeliveryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true)
public class RegisterFulfillmentDeliveryService implements RegisterFulfillmentDeliveryUseCase {
    private final RegisterFulfillmentDeliveryPort registerFulfillmentDeliveryPort;
    private final SaveFulfillmentDeliveryRegistrationPort saveFulfillmentDeliveryRegistrationPort;

    @Override
    public RegisterFulfillmentDeliveryResult registerDelivery(RegisterFulfillmentDeliveryCommand command) {
        return registerFulfillmentDeliveryPort.registerDelivery(command);
    }

    @Override
    @Transactional(isolation = READ_COMMITTED)
    public RegisterFulfillmentDeliveryResult registerDeliveryIdempotent(RegisterFulfillmentDeliveryCommand command) {
        FulfillmentDeliveryRegistration registration = FulfillmentDeliveryRegistration.from(
                FulfillmentCommandToStateMapper.mapToDeliveryRegistrationCreateState(command)
        );
        saveFulfillmentDeliveryRegistrationPort.save(registration);

        return registerFulfillmentDeliveryPort.registerDelivery(command);
    }
}
