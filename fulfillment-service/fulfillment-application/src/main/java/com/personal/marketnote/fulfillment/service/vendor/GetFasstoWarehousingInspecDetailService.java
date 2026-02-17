package com.personal.marketnote.fulfillment.service.vendor;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.fulfillment.mapper.FasstoWarehousingCommandToRequestMapper;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFasstoWarehousingInspecDetailCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFasstoWarehousingInspecDetailResult;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.GetFasstoWarehousingInspecDetailUseCase;
import com.personal.marketnote.fulfillment.port.out.vendor.GetFasstoWarehousingInspecDetailPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true)
public class GetFasstoWarehousingInspecDetailService implements GetFasstoWarehousingInspecDetailUseCase {
    private final GetFasstoWarehousingInspecDetailPort getFasstoWarehousingInspecDetailPort;

    @Override
    public GetFasstoWarehousingInspecDetailResult getWarehousingInspecDetail(GetFasstoWarehousingInspecDetailCommand command) {
        return getFasstoWarehousingInspecDetailPort.getWarehousingInspecDetail(
                FasstoWarehousingCommandToRequestMapper.mapToInspecDetailQuery(command)
        );
    }
}
