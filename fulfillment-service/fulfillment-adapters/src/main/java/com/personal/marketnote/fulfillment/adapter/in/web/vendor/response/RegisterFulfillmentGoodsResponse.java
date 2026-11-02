package com.personal.marketnote.fulfillment.adapter.in.web.vendor.response;

import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFulfillmentGoodsItemResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFulfillmentGoodsResult;

import java.util.List;

public record RegisterFulfillmentGoodsResponse(
        Integer dataCount,
        List<RegisterFulfillmentGoodsItemResult> goods
) {
    public static RegisterFulfillmentGoodsResponse from(RegisterFulfillmentGoodsResult result) {
        return new RegisterFulfillmentGoodsResponse(result.dataCount(), result.goods());
    }
}
