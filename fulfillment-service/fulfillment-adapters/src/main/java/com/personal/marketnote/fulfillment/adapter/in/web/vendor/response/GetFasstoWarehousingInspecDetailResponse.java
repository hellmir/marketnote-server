package com.personal.marketnote.fulfillment.adapter.in.web.vendor.response;

import com.personal.marketnote.fulfillment.port.in.result.vendor.FasstoWarehousingInspecDetailInfoResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFasstoWarehousingInspecDetailResult;

import java.util.List;

public record GetFasstoWarehousingInspecDetailResponse(
        Integer dataCount,
        List<FasstoWarehousingInspecDetailInfoResult> details
) {
    public static GetFasstoWarehousingInspecDetailResponse from(GetFasstoWarehousingInspecDetailResult result) {
        return new GetFasstoWarehousingInspecDetailResponse(result.dataCount(), result.details());
    }
}
