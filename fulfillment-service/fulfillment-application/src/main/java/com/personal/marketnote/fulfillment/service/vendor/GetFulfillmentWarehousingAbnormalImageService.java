package com.personal.marketnote.fulfillment.service.vendor;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.fulfillment.mapper.FulfillmentWarehousingCommandToRequestMapper;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentWarehousingAbnormalImageCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentWarehousingAbnormalImageResult;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.GetFulfillmentWarehousingAbnormalImageUseCase;
import com.personal.marketnote.fulfillment.port.out.vendor.GetFulfillmentWarehousingAbnormalImagePort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true)
public class GetFulfillmentWarehousingAbnormalImageService implements GetFulfillmentWarehousingAbnormalImageUseCase {
    private final GetFulfillmentWarehousingAbnormalImagePort getFulfillmentWarehousingAbnormalImagePort;

    @Override
    public GetFulfillmentWarehousingAbnormalImageResult getWarehousingAbnormalImage(
            GetFulfillmentWarehousingAbnormalImageCommand command
    ) {
        return getFulfillmentWarehousingAbnormalImagePort.getWarehousingAbnormalImage(
                FulfillmentWarehousingCommandToRequestMapper.mapToAbnormalImageQuery(command)
        );
    }
}
