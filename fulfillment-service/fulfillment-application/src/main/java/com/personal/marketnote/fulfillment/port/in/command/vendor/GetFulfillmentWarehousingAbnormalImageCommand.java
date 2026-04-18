package com.personal.marketnote.fulfillment.port.in.command.vendor;

public record GetFulfillmentWarehousingAbnormalImageCommand(
        String accessToken,
        String slipNumber,
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
        return new GetFulfillmentWarehousingAbnormalImageCommand(accessToken, slipNumber, productCode, goodsSerialNo, fileSeq, imageNumber);
    }
}
