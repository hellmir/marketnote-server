package com.personal.marketnote.fulfillment.service.vendor;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.fulfillment.mapper.FasstoReturnDeliveryCommandToRequestMapper;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFasstoReturnGodDetailCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFasstoReturnGodDetailResult;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.GetFasstoReturnGodDetailUseCase;
import com.personal.marketnote.fulfillment.port.out.vendor.GetFasstoReturnGodDetailPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true)
public class GetFasstoReturnGodDetailService implements GetFasstoReturnGodDetailUseCase {
    private final GetFasstoReturnGodDetailPort getFasstoReturnGodDetailPort;

    @Override
    public GetFasstoReturnGodDetailResult getReturnGodDetail(GetFasstoReturnGodDetailCommand command) {
        return getFasstoReturnGodDetailPort.getReturnGodDetail(
                FasstoReturnDeliveryCommandToRequestMapper.mapToReturnGodDetailQuery(command)
        );
    }
}
