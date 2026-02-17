package com.personal.marketnote.fulfillment.port.out.vendor;

import com.personal.marketnote.fulfillment.domain.vendor.fassto.delivery.FasstoDeliveryOutOrdGoodsByOrdNoQuery;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFasstoDeliveryOutOrdGoodsByOrdNoResult;

public interface GetFasstoDeliveryOutOrdGoodsByOrdNoPort {
    GetFasstoDeliveryOutOrdGoodsByOrdNoResult getOutOrdGoodsByOrdNo(FasstoDeliveryOutOrdGoodsByOrdNoQuery query);
}
