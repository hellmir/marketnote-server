package com.personal.marketnote.fulfillment.port.in.usecase;

import com.personal.marketnote.fulfillment.port.in.command.CancelInternalFulfillmentDeliveryCommand;
import com.personal.marketnote.fulfillment.port.in.result.CancelInternalFulfillmentDeliveryResult;

public interface CancelInternalFulfillmentDeliveryUseCase {
    CancelInternalFulfillmentDeliveryResult cancelDelivery(CancelInternalFulfillmentDeliveryCommand command);
}
