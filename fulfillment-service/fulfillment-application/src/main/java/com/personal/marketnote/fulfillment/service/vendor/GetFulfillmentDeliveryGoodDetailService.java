package com.personal.marketnote.fulfillment.service.vendor;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentDeliveryGoodDetailCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentDeliveryGoodDetailResult;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.GetFulfillmentDeliveryGoodDetailUseCase;
import com.personal.marketnote.fulfillment.port.out.vendor.GetFulfillmentDeliveryGoodDetailPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true)
public class GetFulfillmentDeliveryGoodDetailService implements GetFulfillmentDeliveryGoodDetailUseCase {
    private final GetFulfillmentDeliveryGoodDetailPort getFulfillmentDeliveryGoodDetailPort;

    @Override
    public GetFulfillmentDeliveryGoodDetailResult getDeliveryGoodDetail(
            GetFulfillmentDeliveryGoodDetailCommand command
    ) {
        return getFulfillmentDeliveryGoodDetailPort.getDeliveryGoodDetail(command);
    }
}
