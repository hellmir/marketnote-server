package com.personal.marketnote.fulfillment.port.in.usecase.vendor;

import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFasstoDeliveryIcsCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFasstoDeliveryResult;

public interface RegisterFasstoDeliveryIcsUseCase {
    RegisterFasstoDeliveryResult registerDeliveryIcs(RegisterFasstoDeliveryIcsCommand command);
}
