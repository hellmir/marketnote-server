package com.personal.marketnote.fulfillment.port.in.command.vendor;

public record GetFulfillmentWarehousingAbnormalImageCommand(
        String accessToken,
        String slipNo,
        String godCd,
        String goodsSerialNo,
        String fileSeq,
        String imgNo
) {
    public static GetFulfillmentWarehousingAbnormalImageCommand of(
            String accessToken,
            String slipNo,
            String godCd,
            String goodsSerialNo,
            String fileSeq,
            String imgNo
    ) {
        return new GetFulfillmentWarehousingAbnormalImageCommand(accessToken, slipNo, godCd, goodsSerialNo, fileSeq, imgNo);
    }
}
