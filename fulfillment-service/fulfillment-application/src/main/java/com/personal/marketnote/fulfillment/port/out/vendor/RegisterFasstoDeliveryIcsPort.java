package com.personal.marketnote.fulfillment.port.out.vendor;

import com.personal.marketnote.fulfillment.domain.vendor.fassto.delivery.FasstoDeliveryIcsMapper;
import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFasstoDeliveryResult;

public interface RegisterFasstoDeliveryIcsPort {
    RegisterFasstoDeliveryResult registerDeliveryIcs(FasstoDeliveryIcsMapper request);
}
