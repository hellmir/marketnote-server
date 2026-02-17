package com.personal.marketnote.fulfillment.port.in.usecase.vendor;

import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFasstoDeliveryOutOrdGoodsByOrdNoCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFasstoDeliveryOutOrdGoodsByOrdNoResult;

public interface GetFasstoDeliveryOutOrdGoodsByOrdNoUseCase {
    GetFasstoDeliveryOutOrdGoodsByOrdNoResult getOutOrdGoodsByOrdNo(GetFasstoDeliveryOutOrdGoodsByOrdNoCommand command);
}
