package com.personal.marketnote.fulfillment.port.out.vendor;

import com.personal.marketnote.fulfillment.domain.vendor.fassto.returndelivery.FasstoDirectReturnDeliveryMapper;
import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFasstoDeliveryResult;

public interface RegisterFasstoDirectReturnDeliveryPort {
    RegisterFasstoDeliveryResult registerDirectReturnDelivery(FasstoDirectReturnDeliveryMapper request);
}
