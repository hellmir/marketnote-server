package com.personal.marketnote.fulfillment.service.vendor;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.fulfillment.mapper.FulfillmentShopCommandToRequestMapper;
import com.personal.marketnote.fulfillment.port.in.command.vendor.UpdateFulfillmentShopCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.UpdateFulfillmentShopResult;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.UpdateFulfillmentShopUseCase;
import com.personal.marketnote.fulfillment.port.out.vendor.UpdateFulfillmentShopPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true)
public class UpdateFulfillmentShopService implements UpdateFulfillmentShopUseCase {
    private final UpdateFulfillmentShopPort updateFulfillmentShopPort;

    @Override
    public UpdateFulfillmentShopResult updateShop(UpdateFulfillmentShopCommand command) {
        return updateFulfillmentShopPort.updateShop(
                FulfillmentShopCommandToRequestMapper.mapToUpdateRequest(command)
        );
    }
}
