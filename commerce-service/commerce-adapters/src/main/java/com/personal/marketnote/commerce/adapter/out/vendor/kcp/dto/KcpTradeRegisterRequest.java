package com.personal.marketnote.commerce.adapter.out.vendor.kcp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record KcpTradeRegisterRequest(
        @JsonProperty("site_cd") String siteCd,
        @JsonProperty("ordr_idxx") String ordrIdxx,
        @JsonProperty("good_mny") String goodMny,
        @JsonProperty("pay_method") String payMethod,
        @JsonProperty("good_name") String goodName,
        @JsonProperty("Ret_URL") String retUrl
) {
}
