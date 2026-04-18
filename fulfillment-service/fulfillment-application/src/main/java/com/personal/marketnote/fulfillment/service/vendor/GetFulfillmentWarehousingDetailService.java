package com.personal.marketnote.fulfillment.service.vendor;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentWarehousingDetailCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentWarehousingDetailResult;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.GetFulfillmentWarehousingDetailUseCase;
import com.personal.marketnote.fulfillment.port.out.vendor.GetFulfillmentWarehousingDetailPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true)
public class GetFulfillmentWarehousingDetailService implements GetFulfillmentWarehousingDetailUseCase {
    private final GetFulfillmentWarehousingDetailPort getFulfillmentWarehousingDetailPort;

    @Override
    public GetFulfillmentWarehousingDetailResult getWarehousingDetail(GetFulfillmentWarehousingDetailCommand command) {
        return getFulfillmentWarehousingDetailPort.getWarehousingDetail(command);
    }
}
