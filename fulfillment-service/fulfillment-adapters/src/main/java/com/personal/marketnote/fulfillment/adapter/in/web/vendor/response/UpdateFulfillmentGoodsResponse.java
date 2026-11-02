package com.personal.marketnote.fulfillment.adapter.in.web.vendor.response;

import com.personal.marketnote.fulfillment.port.in.result.vendor.UpdateFulfillmentGoodsItemResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.UpdateFulfillmentGoodsResult;

import java.util.List;

public record UpdateFulfillmentGoodsResponse(
        Integer dataCount,
        List<UpdateFulfillmentGoodsItemResult> goods
) {
    public static UpdateFulfillmentGoodsResponse from(UpdateFulfillmentGoodsResult result) {
        return new UpdateFulfillmentGoodsResponse(result.dataCount(), result.goods());
    }
}
