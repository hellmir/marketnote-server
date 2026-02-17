package com.personal.marketnote.fulfillment.port.in.usecase.vendor;

import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFasstoDeliveryCarCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFasstoDeliveryResult;

public interface RegisterFasstoDeliveryCarUseCase {
    RegisterFasstoDeliveryResult registerDeliveryCar(RegisterFasstoDeliveryCarCommand command);
}
