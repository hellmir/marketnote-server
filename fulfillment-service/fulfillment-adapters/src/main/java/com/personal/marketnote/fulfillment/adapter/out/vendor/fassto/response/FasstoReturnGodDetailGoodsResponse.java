package com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FasstoReturnGodDetailGoodsResponse(
        String cstGodCd,
        String godNm,
        String makeDt,
        String distTermDt,
        String inQty,
        String remark,
        String rtnGodCheckStat,
        String rtnGodCheckStatNm
) {
}
