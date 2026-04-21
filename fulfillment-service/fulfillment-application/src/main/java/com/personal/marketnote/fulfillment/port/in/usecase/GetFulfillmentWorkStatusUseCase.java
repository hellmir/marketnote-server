package com.personal.marketnote.fulfillment.port.in.usecase;

import com.personal.marketnote.fulfillment.port.in.command.GetFulfillmentWorkStatusCommand;
import com.personal.marketnote.fulfillment.port.in.result.GetFulfillmentWorkStatusResult;

public interface GetFulfillmentWorkStatusUseCase {
    GetFulfillmentWorkStatusResult getWorkStatus(GetFulfillmentWorkStatusCommand command);
}
