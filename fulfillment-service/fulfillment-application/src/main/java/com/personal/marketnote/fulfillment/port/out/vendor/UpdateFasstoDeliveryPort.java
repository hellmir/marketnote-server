package com.personal.marketnote.fulfillment.port.out.vendor;

import com.personal.marketnote.fulfillment.domain.vendor.fassto.delivery.FasstoDeliveryMapper;
import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFasstoDeliveryResult;

public interface UpdateFasstoDeliveryPort {
    RegisterFasstoDeliveryResult updateDelivery(FasstoDeliveryMapper request);
}
