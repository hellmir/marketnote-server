package com.personal.marketnote.fulfillment.port.out.vendor;

import com.personal.marketnote.fulfillment.domain.vendor.fassto.delivery.FasstoDeliveryCarMapper;
import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFasstoDeliveryResult;

public interface UpdateFasstoDeliveryCarPort {
    RegisterFasstoDeliveryResult updateDeliveryCar(FasstoDeliveryCarMapper request);
}
