package com.personal.marketnote.fulfillment.port.in.command.vendor;

import lombok.Builder;

import java.util.UUID;

@Builder
public record GetFulfillmentWarehousingAbnormalImageCommand(
    String accessToken,
    String slipNumber,
    UUID productKey,
    String productCode,
    String fasstoProductCode,
    String goodsSerialNo,
    String fileSeq,
    String imageNumber
) {
}
