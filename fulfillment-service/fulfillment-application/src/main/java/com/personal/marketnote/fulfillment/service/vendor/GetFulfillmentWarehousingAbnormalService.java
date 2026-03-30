package com.personal.marketnote.fulfillment.service.vendor;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.fulfillment.mapper.FulfillmentWarehousingCommandToRequestMapper;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentWarehousingAbnormalCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentWarehousingAbnormalResult;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.GetFulfillmentWarehousingAbnormalUseCase;
import com.personal.marketnote.fulfillment.port.out.vendor.GetFulfillmentWarehousingAbnormalPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true)
public class GetFulfillmentWarehousingAbnormalService implements GetFulfillmentWarehousingAbnormalUseCase {
    private final GetFulfillmentWarehousingAbnormalPort getFulfillmentWarehousingAbnormalPort;

    @Override
    public GetFulfillmentWarehousingAbnormalResult getWarehousingAbnormal(GetFulfillmentWarehousingAbnormalCommand command) {
        return getFulfillmentWarehousingAbnormalPort.getWarehousingAbnormal(
                FulfillmentWarehousingCommandToRequestMapper.mapToAbnormalQuery(command)
        );
    }
}
