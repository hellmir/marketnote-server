package com.personal.marketnote.fulfillment.port.in.usecase.vendor;

import com.personal.marketnote.fulfillment.port.in.command.vendor.UpdateFasstoDeliveryCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFasstoDeliveryResult;

public interface UpdateFasstoDeliveryUseCase {
    RegisterFasstoDeliveryResult updateDelivery(UpdateFasstoDeliveryCommand command);
}
