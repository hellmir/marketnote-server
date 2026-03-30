package com.personal.marketnote.fulfillment.adapter.in.web.vendor.response;

import com.personal.marketnote.fulfillment.port.in.result.vendor.FulfillmentDeliveryOutOrdGoodsInfoResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentDeliveryOutOrdGoodsDetailResult;

import java.util.List;

public record GetFulfillmentDeliveryOutOrdGoodsDetailResponse(
        Integer dataCount,
        List<FulfillmentDeliveryOutOrdGoodsInfoResult> goodsByInvoice
) {
    public static GetFulfillmentDeliveryOutOrdGoodsDetailResponse from(GetFulfillmentDeliveryOutOrdGoodsDetailResult result) {
        return new GetFulfillmentDeliveryOutOrdGoodsDetailResponse(result.dataCount(), result.goodsByInvoice());
    }
}
