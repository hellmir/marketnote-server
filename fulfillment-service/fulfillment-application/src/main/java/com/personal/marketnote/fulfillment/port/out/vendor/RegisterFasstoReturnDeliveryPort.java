package com.personal.marketnote.fulfillment.port.out.vendor;

import com.personal.marketnote.fulfillment.domain.vendor.fassto.returndelivery.FasstoReturnDeliveryMapper;
import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFasstoDeliveryResult;

public interface RegisterFasstoReturnDeliveryPort {
    RegisterFasstoDeliveryResult registerReturnDelivery(FasstoReturnDeliveryMapper request);
}
