package com.personal.marketnote.fulfillment.service.vendor;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.fulfillment.mapper.FulfillmentWarehousingCommandToRequestMapper;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentWarehousingCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentWarehousingResult;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.GetFulfillmentWarehousingUseCase;
import com.personal.marketnote.fulfillment.port.out.vendor.GetFulfillmentWarehousingPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true)
public class GetFulfillmentWarehousingService implements GetFulfillmentWarehousingUseCase {
    private final GetFulfillmentWarehousingPort getFulfillmentWarehousingPort;

    @Override
    public GetFulfillmentWarehousingResult getWarehousing(GetFulfillmentWarehousingCommand command) {
        return getFulfillmentWarehousingPort.getWarehousing(
                FulfillmentWarehousingCommandToRequestMapper.mapToQuery(command)
        );
    }
}
