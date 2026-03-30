package com.personal.marketnote.fulfillment.service.vendor;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.fulfillment.mapper.FulfillmentDeliveryCommandToRequestMapper;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentDeliveriesCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentDeliveriesResult;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.GetFulfillmentDeliveriesUseCase;
import com.personal.marketnote.fulfillment.port.out.vendor.GetFulfillmentDeliveriesPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true)
public class GetFulfillmentDeliveriesService implements GetFulfillmentDeliveriesUseCase {
    private final GetFulfillmentDeliveriesPort getFulfillmentDeliveriesPort;

    @Override
    public GetFulfillmentDeliveriesResult getDeliveries(GetFulfillmentDeliveriesCommand command) {
        return getFulfillmentDeliveriesPort.getDeliveries(
                FulfillmentDeliveryCommandToRequestMapper.mapToQuery(command)
        );
    }
}
