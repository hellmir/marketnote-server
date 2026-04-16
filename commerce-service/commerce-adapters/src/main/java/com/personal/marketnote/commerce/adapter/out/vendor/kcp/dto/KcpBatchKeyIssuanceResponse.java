package com.personal.marketnote.commerce.adapter.out.vendor.kcp.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KcpBatchKeyIssuanceResponse(
        @JsonProperty("res_cd") String resCd,
        @JsonProperty("res_msg") String resMsg,
        @JsonProperty("batch_key") String batchKey,
        @JsonProperty("card_cd") String cardCd,
        @JsonProperty("card_name") String cardName,
        @JsonProperty("card_bin_type_01") String cardBinType01,
        @JsonProperty("card_bin_type_02") String cardBinType02
) {
    public boolean isSuccess() {
        return "0000".equals(resCd);
    }
}
