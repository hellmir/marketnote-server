package com.personal.marketnote.fulfillment.service.vendor;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentDeliveryOutOrdGoodsDetailCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentDeliveryOutOrdGoodsDetailResult;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.GetFulfillmentDeliveryOutOrdGoodsDetailUseCase;
import com.personal.marketnote.fulfillment.port.out.vendor.GetFulfillmentDeliveryOutOrdGoodsDetailPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true)
public class GetFulfillmentDeliveryOutOrdGoodsDetailService implements GetFulfillmentDeliveryOutOrdGoodsDetailUseCase {
    private final GetFulfillmentDeliveryOutOrdGoodsDetailPort getFulfillmentDeliveryOutOrdGoodsDetailPort;

    @Override
    public GetFulfillmentDeliveryOutOrdGoodsDetailResult getOutOrdGoodsDetail(GetFulfillmentDeliveryOutOrdGoodsDetailCommand command) {
        return getFulfillmentDeliveryOutOrdGoodsDetailPort.getOutOrdGoodsDetail(command);
    }
}
