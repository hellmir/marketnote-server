package com.personal.marketnote.fulfillment.service.vendor;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentDeliveryDetailCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentDeliveryDetailResult;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.GetFulfillmentDeliveryDetailUseCase;
import com.personal.marketnote.fulfillment.port.out.vendor.GetFulfillmentDeliveryDetailPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true)
public class GetFulfillmentDeliveryDetailService implements GetFulfillmentDeliveryDetailUseCase {
    private final GetFulfillmentDeliveryDetailPort getFulfillmentDeliveryDetailPort;

    @Override
    public GetFulfillmentDeliveryDetailResult getDeliveryDetail(GetFulfillmentDeliveryDetailCommand command) {
        return getFulfillmentDeliveryDetailPort.getDeliveryDetail(command);
    }
}
