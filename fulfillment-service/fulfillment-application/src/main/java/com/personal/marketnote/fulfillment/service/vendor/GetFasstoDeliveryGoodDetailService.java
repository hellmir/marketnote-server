package com.personal.marketnote.fulfillment.service.vendor;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.fulfillment.mapper.FasstoDeliveryCommandToRequestMapper;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFasstoDeliveryGoodDetailCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFasstoDeliveryGoodDetailResult;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.GetFasstoDeliveryGoodDetailUseCase;
import com.personal.marketnote.fulfillment.port.out.vendor.GetFasstoDeliveryGoodDetailPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true)
public class GetFasstoDeliveryGoodDetailService implements GetFasstoDeliveryGoodDetailUseCase {
    private final GetFasstoDeliveryGoodDetailPort getFasstoDeliveryGoodDetailPort;

    @Override
    public GetFasstoDeliveryGoodDetailResult getDeliveryGoodDetail(
            GetFasstoDeliveryGoodDetailCommand command
    ) {
        return getFasstoDeliveryGoodDetailPort.getDeliveryGoodDetail(
                FasstoDeliveryCommandToRequestMapper.mapToDeliveryGoodDetailQuery(command)
        );
    }
}
