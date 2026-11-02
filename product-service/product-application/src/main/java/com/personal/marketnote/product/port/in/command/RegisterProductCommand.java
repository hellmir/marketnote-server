package com.personal.marketnote.product.port.in.command;

import lombok.Builder;

import java.util.List;

@Builder
public record RegisterProductCommand(
        Long sellerId,
        String name,
        String brandName,
        String detail,
        Long price,
        Long discountPrice,
        Long accumulatedPoint,
        Boolean isFindAllOptions,
        List<String> tags,
        FulfillmentVendorGoodsOptionCommand fulfillmentVendorGoods
) {
}
