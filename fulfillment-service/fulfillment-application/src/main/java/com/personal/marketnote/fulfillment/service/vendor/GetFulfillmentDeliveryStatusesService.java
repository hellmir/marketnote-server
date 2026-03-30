package com.personal.marketnote.fulfillment.service.vendor;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.fulfillment.mapper.FulfillmentDeliveryCommandToRequestMapper;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentDeliveryStatusesCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentDeliveryStatusesResult;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.GetFulfillmentDeliveryStatusesUseCase;
import com.personal.marketnote.fulfillment.port.out.vendor.GetFulfillmentDeliveryStatusesPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true)
public class GetFulfillmentDeliveryStatusesService implements GetFulfillmentDeliveryStatusesUseCase {
    private final GetFulfillmentDeliveryStatusesPort getFulfillmentDeliveryStatusesPort;

    @Override
    public GetFulfillmentDeliveryStatusesResult getDeliveryStatuses(GetFulfillmentDeliveryStatusesCommand command) {
        return getFulfillmentDeliveryStatusesPort.getDeliveryStatuses(
                FulfillmentDeliveryCommandToRequestMapper.mapToDeliveryStatusQuery(command)
        );
    }
}
