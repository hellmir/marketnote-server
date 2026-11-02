package com.personal.marketnote.fulfillment.service.vendor;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentDeliveryOutOrdGoodsByOrdNoCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentDeliveryOutOrdGoodsByOrdNoResult;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.GetFulfillmentDeliveryOutOrdGoodsByOrdNoUseCase;
import com.personal.marketnote.fulfillment.port.out.vendor.GetFulfillmentDeliveryOutOrdGoodsByOrdNoPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true)
public class GetFulfillmentDeliveryOutOrdGoodsByOrdNoService implements GetFulfillmentDeliveryOutOrdGoodsByOrdNoUseCase {
    private final GetFulfillmentDeliveryOutOrdGoodsByOrdNoPort getFulfillmentDeliveryOutOrdGoodsByOrdNoPort;

    @Override
    public GetFulfillmentDeliveryOutOrdGoodsByOrdNoResult getOutOrdGoodsByOrdNo(
            GetFulfillmentDeliveryOutOrdGoodsByOrdNoCommand command
    ) {
        return getFulfillmentDeliveryOutOrdGoodsByOrdNoPort.getOutOrdGoodsByOrdNo(command);
    }
}
