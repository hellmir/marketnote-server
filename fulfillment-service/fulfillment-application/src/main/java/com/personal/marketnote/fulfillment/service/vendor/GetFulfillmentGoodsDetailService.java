package com.personal.marketnote.fulfillment.service.vendor;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentGoodsDetailCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentGoodsResult;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.GetFulfillmentGoodsDetailUseCase;
import com.personal.marketnote.fulfillment.port.out.vendor.GetFulfillmentGoodsDetailPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true)
public class GetFulfillmentGoodsDetailService implements GetFulfillmentGoodsDetailUseCase {
    private final GetFulfillmentGoodsDetailPort getFulfillmentGoodsDetailPort;

    @Override
    public GetFulfillmentGoodsResult getGoodsDetail(GetFulfillmentGoodsDetailCommand command) {
        return getFulfillmentGoodsDetailPort.getGoodsDetail(command);
    }
}
