package com.personal.marketnote.fulfillment.port.in.command.vendor;

public record GetFasstoWarehousingAbnormalImageCommand(
        String accessToken,
        String slipNo,
        String godCd,
        String goodsSerialNo,
        String fileSeq,
        String imgNo
) {
    public static GetFasstoWarehousingAbnormalImageCommand of(
            String accessToken,
            String slipNo,
            String godCd,
            String goodsSerialNo,
            String fileSeq,
            String imgNo
    ) {
        return new GetFasstoWarehousingAbnormalImageCommand(accessToken, slipNo, godCd, goodsSerialNo, fileSeq, imgNo);
    }
}
