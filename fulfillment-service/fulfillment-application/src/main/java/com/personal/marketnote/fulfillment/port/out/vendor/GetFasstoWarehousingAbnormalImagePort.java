package com.personal.marketnote.fulfillment.port.out.vendor;

import com.personal.marketnote.fulfillment.domain.vendor.fassto.warehousing.FasstoWarehousingAbnormalImageQuery;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFasstoWarehousingAbnormalImageResult;

public interface GetFasstoWarehousingAbnormalImagePort {
    GetFasstoWarehousingAbnormalImageResult getWarehousingAbnormalImage(FasstoWarehousingAbnormalImageQuery query);
}
