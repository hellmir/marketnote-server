package com.personal.marketnote.fulfillment.adapter.in.web.vendor.response;

import com.personal.marketnote.fulfillment.port.in.result.vendor.FasstoReturnGodDetailInfoResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFasstoReturnGodDetailResult;

import java.util.List;

public record GetFasstoReturnGodDetailResponse(
        Integer dataCount,
        List<FasstoReturnGodDetailInfoResult> returnGodInfos
) {
    public static GetFasstoReturnGodDetailResponse from(GetFasstoReturnGodDetailResult result) {
        return new GetFasstoReturnGodDetailResponse(result.dataCount(), result.returnGodInfos());
    }
}
