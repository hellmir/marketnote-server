package com.personal.marketnote.fulfillment.service.vendor;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.fulfillment.mapper.FulfillmentDirectReturnDeliveryCommandToRequestMapper;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentDirectReturnDeliveryCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFulfillmentDeliveryResult;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.RegisterFulfillmentDirectReturnDeliveryUseCase;
import com.personal.marketnote.fulfillment.port.out.vendor.RegisterFulfillmentDirectReturnDeliveryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true)
public class RegisterFulfillmentDirectReturnDeliveryService implements RegisterFulfillmentDirectReturnDeliveryUseCase {
    private final RegisterFulfillmentDirectReturnDeliveryPort registerFulfillmentDirectReturnDeliveryPort;

    @Override
    public RegisterFulfillmentDeliveryResult registerDirectReturnDelivery(RegisterFulfillmentDirectReturnDeliveryCommand command) {
        return registerFulfillmentDirectReturnDeliveryPort.registerDirectReturnDelivery(
                FulfillmentDirectReturnDeliveryCommandToRequestMapper.mapToRegisterRequest(command)
        );
    }
}
