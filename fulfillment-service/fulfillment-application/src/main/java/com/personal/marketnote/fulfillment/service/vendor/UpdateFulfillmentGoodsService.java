package com.personal.marketnote.fulfillment.service.vendor;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.fulfillment.port.in.command.vendor.UpdateFulfillmentGoodsCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.UpdateFulfillmentGoodsResult;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.UpdateFulfillmentGoodsUseCase;
import com.personal.marketnote.fulfillment.port.out.vendor.UpdateFulfillmentGoodsPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true)
public class UpdateFulfillmentGoodsService implements UpdateFulfillmentGoodsUseCase {
    private final UpdateFulfillmentGoodsPort updateFulfillmentGoodsPort;

    @Override
    public UpdateFulfillmentGoodsResult updateGoods(UpdateFulfillmentGoodsCommand command) {
        return updateFulfillmentGoodsPort.updateGoods(command);
    }
}
