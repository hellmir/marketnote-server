package com.personal.marketnote.product.port.in.command;

import lombok.Builder;

import java.util.List;

@Builder
public record UpdateProductCommand(
        Long id,
        String name,
        String brandName,
        String detail,
        Boolean isFindAllOptions,
        List<String> tags,
        FulfillmentVendorGoodsOptionCommand fulfillmentVendorGoods
) {
}
