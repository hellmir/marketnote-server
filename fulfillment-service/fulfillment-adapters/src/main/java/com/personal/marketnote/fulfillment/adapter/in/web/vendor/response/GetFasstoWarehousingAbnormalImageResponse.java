package com.personal.marketnote.fulfillment.adapter.in.web.vendor.response;

import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFasstoWarehousingAbnormalImageResult;

public record GetFasstoWarehousingAbnormalImageResponse(
        Integer dataCount,
        Object data
) {
    public static GetFasstoWarehousingAbnormalImageResponse from(GetFasstoWarehousingAbnormalImageResult result) {
        return new GetFasstoWarehousingAbnormalImageResponse(result.dataCount(), result.data());
    }
}
