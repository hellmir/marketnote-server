package com.personal.marketnote.fulfillment.service.vendor;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.fulfillment.domain.delivery.FulfillmentDeliveryRegistration;
import com.personal.marketnote.fulfillment.mapper.FasstoCommandToStateMapper;
import com.personal.marketnote.fulfillment.mapper.FulfillmentDeliveryCommandToRequestMapper;
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
        return registerFulfillmentDeliveryPort.registerDelivery(
                FulfillmentDeliveryCommandToRequestMapper.mapToRegisterRequest(command)
        );
    }

    @Override
    @Transactional(isolation = READ_COMMITTED)
    public RegisterFulfillmentDeliveryResult registerDeliveryIdempotent(RegisterFulfillmentDeliveryCommand command) {
        FulfillmentDeliveryRegistration registration = FulfillmentDeliveryRegistration.from(
                FasstoCommandToStateMapper.mapToDeliveryRegistrationCreateState(command)
        );
        saveFulfillmentDeliveryRegistrationPort.save(registration);

        return registerFulfillmentDeliveryPort.registerDelivery(
                FulfillmentDeliveryCommandToRequestMapper.mapToRegisterRequest(command)
        );
    }
}
