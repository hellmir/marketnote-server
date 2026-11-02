package com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FulfillmentWarehousingAbnormalItemResponse(
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
}
