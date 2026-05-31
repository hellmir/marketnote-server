package com.personal.marketnote.fulfillment.port.in.usecase;

import com.personal.marketnote.fulfillment.port.in.command.RegisterInternalReturnDeliveryCommand;
import com.personal.marketnote.fulfillment.port.in.result.RegisterInternalReturnDeliveryResult;

public interface RegisterInternalReturnDeliveryUseCase {
    RegisterInternalReturnDeliveryResult registerReturnDelivery(RegisterInternalReturnDeliveryCommand command);
}
