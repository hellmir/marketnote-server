package com.personal.marketnote.fulfillment.service.vendor;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.fulfillment.mapper.FasstoWarehousingCommandToRequestMapper;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFasstoWarehousingAbnormalImageCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFasstoWarehousingAbnormalImageResult;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.GetFasstoWarehousingAbnormalImageUseCase;
import com.personal.marketnote.fulfillment.port.out.vendor.GetFasstoWarehousingAbnormalImagePort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true)
public class GetFasstoWarehousingAbnormalImageService implements GetFasstoWarehousingAbnormalImageUseCase {
    private final GetFasstoWarehousingAbnormalImagePort getFasstoWarehousingAbnormalImagePort;

    @Override
    public GetFasstoWarehousingAbnormalImageResult getWarehousingAbnormalImage(
            GetFasstoWarehousingAbnormalImageCommand command
    ) {
        return getFasstoWarehousingAbnormalImagePort.getWarehousingAbnormalImage(
                FasstoWarehousingCommandToRequestMapper.mapToAbnormalImageQuery(command)
        );
    }
}
