package com.personal.marketnote.fulfillment.port.out.vendor;

import com.personal.marketnote.fulfillment.domain.vendor.fassto.delivery.FasstoDeliveryGoodDetailQuery;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFasstoDeliveryGoodDetailResult;

public interface GetFasstoDeliveryGoodDetailPort {
    GetFasstoDeliveryGoodDetailResult getDeliveryGoodDetail(FasstoDeliveryGoodDetailQuery query);
}
