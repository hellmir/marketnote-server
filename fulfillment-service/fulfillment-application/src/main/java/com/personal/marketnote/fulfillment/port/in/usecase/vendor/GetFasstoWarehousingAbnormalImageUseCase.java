package com.personal.marketnote.fulfillment.port.in.usecase.vendor;

import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFasstoWarehousingAbnormalImageCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFasstoWarehousingAbnormalImageResult;

public interface GetFasstoWarehousingAbnormalImageUseCase {
    GetFasstoWarehousingAbnormalImageResult getWarehousingAbnormalImage(GetFasstoWarehousingAbnormalImageCommand command);
}
