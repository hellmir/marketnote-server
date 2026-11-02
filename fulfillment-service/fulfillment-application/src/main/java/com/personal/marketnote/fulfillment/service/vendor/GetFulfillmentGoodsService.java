package com.personal.marketnote.fulfillment.service.vendor;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentGoodsCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentGoodsResult;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.GetFulfillmentGoodsUseCase;
import com.personal.marketnote.fulfillment.port.out.vendor.GetFulfillmentGoodsPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true)
public class GetFulfillmentGoodsService implements GetFulfillmentGoodsUseCase {
    private final GetFulfillmentGoodsPort getFulfillmentGoodsPort;

    @Override
    public GetFulfillmentGoodsResult getGoods(GetFulfillmentGoodsCommand command) {
        return getFulfillmentGoodsPort.getGoods(command);
    }
}
