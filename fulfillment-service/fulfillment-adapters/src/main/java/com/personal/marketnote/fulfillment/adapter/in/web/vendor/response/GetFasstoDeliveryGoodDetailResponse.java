package com.personal.marketnote.fulfillment.adapter.in.web.vendor.response;

import com.personal.marketnote.fulfillment.port.in.result.vendor.FasstoDeliveryGoodDetailInfoResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFasstoDeliveryGoodDetailResult;

import java.util.List;

public record GetFasstoDeliveryGoodDetailResponse(
        Integer dataCount,
        List<FasstoDeliveryGoodDetailInfoResult> goodDetails
) {
    public static GetFasstoDeliveryGoodDetailResponse from(GetFasstoDeliveryGoodDetailResult result) {
        return new GetFasstoDeliveryGoodDetailResponse(result.dataCount(), result.goodDetails());
    }
}
