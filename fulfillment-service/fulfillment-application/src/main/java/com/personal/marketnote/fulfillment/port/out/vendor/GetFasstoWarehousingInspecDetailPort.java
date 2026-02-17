package com.personal.marketnote.fulfillment.port.out.vendor;

import com.personal.marketnote.fulfillment.domain.vendor.fassto.warehousing.FasstoWarehousingInspecDetailQuery;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFasstoWarehousingInspecDetailResult;

public interface GetFasstoWarehousingInspecDetailPort {
    GetFasstoWarehousingInspecDetailResult getWarehousingInspecDetail(FasstoWarehousingInspecDetailQuery query);
}
