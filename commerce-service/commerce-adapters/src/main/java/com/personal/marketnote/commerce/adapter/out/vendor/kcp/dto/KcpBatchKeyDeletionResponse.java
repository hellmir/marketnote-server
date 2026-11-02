package com.personal.marketnote.commerce.adapter.out.vendor.kcp.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KcpBatchKeyDeletionResponse(
        @JsonProperty("res_cd") String resCd,
        @JsonProperty("res_msg") String resMsg
) {
    public boolean isSuccess() {
        return "0000".equals(resCd);
    }
}
