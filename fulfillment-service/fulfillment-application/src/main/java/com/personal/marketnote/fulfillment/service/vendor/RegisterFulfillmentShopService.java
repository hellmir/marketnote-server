package com.personal.marketnote.fulfillment.service.vendor;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentShopCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFulfillmentShopResult;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.RegisterFulfillmentShopUseCase;
import com.personal.marketnote.fulfillment.port.out.vendor.RegisterFulfillmentShopPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true)
public class RegisterFulfillmentShopService implements RegisterFulfillmentShopUseCase {
    private final RegisterFulfillmentShopPort registerFulfillmentShopPort;

    @Override
    public RegisterFulfillmentShopResult registerShop(RegisterFulfillmentShopCommand command) {
        return registerFulfillmentShopPort.registerShop(command);
    }
}
