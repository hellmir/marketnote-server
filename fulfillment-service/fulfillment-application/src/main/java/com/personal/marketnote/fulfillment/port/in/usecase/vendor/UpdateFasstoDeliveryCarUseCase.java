package com.personal.marketnote.fulfillment.port.in.usecase.vendor;

import com.personal.marketnote.fulfillment.port.in.command.vendor.UpdateFasstoDeliveryCarCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFasstoDeliveryResult;

public interface UpdateFasstoDeliveryCarUseCase {
    RegisterFasstoDeliveryResult updateDeliveryCar(UpdateFasstoDeliveryCarCommand command);
}
