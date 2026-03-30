package com.personal.marketnote.fulfillment.port.in.result.vendor;

import java.util.List;

public record FulfillmentWarehousingAbnormalInfoResult(
        String slipNo,
        String goodsSerialNo,
        String goodsSerialStatus,
        String whCd,
        String cstCd,
        String cstNm,
        String godCd,
        String description,
        String remark,
        String fileSeq,
        Integer lastFileSeqNo,
        String regDate,
        String regNM,
        String updDate,
        String updNm,
        String fileNo,
        List<Object> imageUrl
) {
    public static FulfillmentWarehousingAbnormalInfoResult of(
            String slipNo,
            String goodsSerialNo,
            String goodsSerialStatus,
            String whCd,
            String cstCd,
            String cstNm,
            String godCd,
            String description,
            String remark,
            String fileSeq,
            Integer lastFileSeqNo,
            String regDate,
            String regNM,
            String updDate,
            String updNm,
            String fileNo,
            List<Object> imageUrl
    ) {
        return new FulfillmentWarehousingAbnormalInfoResult(
                slipNo,
                goodsSerialNo,
                goodsSerialStatus,
                whCd,
                cstCd,
                cstNm,
                godCd,
                description,
                remark,
                fileSeq,
                lastFileSeqNo,
                regDate,
                regNM,
                updDate,
                updNm,
                fileNo,
                imageUrl
        );
    }
}
