package com.personal.marketnote.fulfillment.adapter.in.web.vendor.response;

import com.personal.marketnote.fulfillment.port.in.result.vendor.FasstoDeliveryOutOrdGoodsByOrdNoInfoResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFasstoDeliveryOutOrdGoodsByOrdNoResult;

import java.util.List;

public record GetFasstoDeliveryOutOrdGoodsByOrdNoResponse(
        Integer dataCount,
        List<FasstoDeliveryOutOrdGoodsByOrdNoInfoResult> goodsByOrdNo
) {
    public static GetFasstoDeliveryOutOrdGoodsByOrdNoResponse from(GetFasstoDeliveryOutOrdGoodsByOrdNoResult result) {
        return new GetFasstoDeliveryOutOrdGoodsByOrdNoResponse(result.dataCount(), result.goodsByOrdNo());
    }
}
