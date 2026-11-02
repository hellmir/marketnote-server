package com.personal.marketnote.fulfillment.service.vendor;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentGoodsElementsCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentGoodsElementsResult;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.GetFulfillmentGoodsElementsUseCase;
import com.personal.marketnote.fulfillment.port.out.vendor.GetFulfillmentGoodsElementsPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true)
public class GetFulfillmentGoodsElementsService implements GetFulfillmentGoodsElementsUseCase {
    private final GetFulfillmentGoodsElementsPort getFulfillmentGoodsElementsPort;

    @Override
    public GetFulfillmentGoodsElementsResult getGoodsElements(GetFulfillmentGoodsElementsCommand command) {
        return getFulfillmentGoodsElementsPort.getGoodsElements(command);
    }
}
