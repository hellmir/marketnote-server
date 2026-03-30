package com.personal.marketnote.fulfillment.service.vendor;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.fulfillment.mapper.FulfillmentReturnDeliveryCommandToRequestMapper;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentReturnGodDetailCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentReturnGodDetailResult;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.GetFulfillmentReturnGodDetailUseCase;
import com.personal.marketnote.fulfillment.port.out.vendor.GetFulfillmentReturnGodDetailPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true)
public class GetFulfillmentReturnGodDetailService implements GetFulfillmentReturnGodDetailUseCase {
    private final GetFulfillmentReturnGodDetailPort getFulfillmentReturnGodDetailPort;

    @Override
    public GetFulfillmentReturnGodDetailResult getReturnGodDetail(GetFulfillmentReturnGodDetailCommand command) {
        return getFulfillmentReturnGodDetailPort.getReturnGodDetail(
                FulfillmentReturnDeliveryCommandToRequestMapper.mapToReturnGodDetailQuery(command)
        );
    }
}
