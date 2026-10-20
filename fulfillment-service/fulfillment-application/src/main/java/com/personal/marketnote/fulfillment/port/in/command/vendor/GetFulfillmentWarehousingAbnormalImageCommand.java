package com.personal.marketnote.fulfillment.port.in.command.vendor;

import java.util.UUID;

public record GetFulfillmentWarehousingAbnormalImageCommand(
        String accessToken,
        String slipNumber,
        UUID productKey,
        String productCode,
        String goodsSerialNo,
        String fileSeq,
        String imageNumber
) {
    public static GetFulfillmentWarehousingAbnormalImageCommand of(
            String accessToken,
            String slipNumber,
            String productCode,
            String goodsSerialNo,
            String fileSeq,
            String imageNumber
    ) {
        return new GetFulfillmentWarehousingAbnormalImageCommand(
                accessToken, slipNumber, null, productCode, goodsSerialNo, fileSeq, imageNumber
        );
    }

    public static GetFulfillmentWarehousingAbnormalImageCommand of(
            String accessToken,
            String slipNumber,
            UUID productKey,
            String productCode,
            String goodsSerialNo,
            String fileSeq,
            String imageNumber
    ) {
        return new GetFulfillmentWarehousingAbnormalImageCommand(
                accessToken, slipNumber, productKey, productCode, goodsSerialNo, fileSeq, imageNumber
        );
    }
}
