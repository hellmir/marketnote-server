package com.personal.marketnote.fulfillment.port.out.vendor;

import com.personal.marketnote.fulfillment.domain.vendor.fassto.returndelivery.FasstoReturnGodDetailQuery;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFasstoReturnGodDetailResult;

public interface GetFasstoReturnGodDetailPort {
    GetFasstoReturnGodDetailResult getReturnGodDetail(FasstoReturnGodDetailQuery query);
}
