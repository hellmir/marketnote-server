package com.personal.marketnote.fulfillment.port.in.usecase.vendor;

import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFasstoWarehousingInspecDetailCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFasstoWarehousingInspecDetailResult;

public interface GetFasstoWarehousingInspecDetailUseCase {
    GetFasstoWarehousingInspecDetailResult getWarehousingInspecDetail(GetFasstoWarehousingInspecDetailCommand command);
}
