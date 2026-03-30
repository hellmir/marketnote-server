package com.personal.marketnote.fulfillment.service.vendor;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.fulfillment.mapper.FulfillmentWarehousingCommandToRequestMapper;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentWarehousingInspecDetailCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentWarehousingInspecDetailResult;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.GetFulfillmentWarehousingInspecDetailUseCase;
import com.personal.marketnote.fulfillment.port.out.vendor.GetFulfillmentWarehousingInspecDetailPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true)
public class GetFulfillmentWarehousingInspecDetailService implements GetFulfillmentWarehousingInspecDetailUseCase {
    private final GetFulfillmentWarehousingInspecDetailPort getFulfillmentWarehousingInspecDetailPort;

    @Override
    public GetFulfillmentWarehousingInspecDetailResult getWarehousingInspecDetail(GetFulfillmentWarehousingInspecDetailCommand command) {
        return getFulfillmentWarehousingInspecDetailPort.getWarehousingInspecDetail(
                FulfillmentWarehousingCommandToRequestMapper.mapToInspecDetailQuery(command)
        );
    }
}
