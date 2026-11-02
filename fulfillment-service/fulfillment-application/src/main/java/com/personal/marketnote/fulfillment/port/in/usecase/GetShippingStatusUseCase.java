package com.personal.marketnote.fulfillment.port.in.usecase;

import com.personal.marketnote.fulfillment.port.in.command.GetShippingStatusCommand;
import com.personal.marketnote.fulfillment.port.in.result.GetShippingStatusResult;

public interface GetShippingStatusUseCase {
    GetShippingStatusResult getShippingStatus(GetShippingStatusCommand command);
}
