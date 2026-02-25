package com.personal.marketnote.commerce.adapter.out.vendor.kcp.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KcpTradeRegisterResponse(
        @JsonProperty("Code") String resCd,
        @JsonProperty("Message") String resMsg,
        @JsonProperty("approvalKey") String approvalKey,
        @JsonProperty("PayUrl") String payUrl,
        @JsonProperty("traceNo") String traceNo,
        @JsonProperty("paymentMethod") String paymentMethod
) {
    public boolean isSuccess() {
        return "0000".equals(resCd);
    }
}
