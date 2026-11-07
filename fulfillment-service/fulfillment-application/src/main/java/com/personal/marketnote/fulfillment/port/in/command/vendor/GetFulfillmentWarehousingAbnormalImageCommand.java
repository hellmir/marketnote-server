package com.personal.marketnote.fulfillment.port.in.command.vendor;

import java.util.UUID;

import lombok.Builder;

@Builder
public record GetFulfillmentWarehousingAbnormalImageCommand(
        String accessToken,
        String slipNumber,
        UUID productKey,
        String productCode,
        String goodsSerialNo,
        String fileSeq,
        String imageNumber
) {
}
