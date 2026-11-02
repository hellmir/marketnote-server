package com.personal.marketnote.commerce.adapter.out.vendor.kcp.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KcpBatchPaymentApprovalResponse(
        @JsonProperty("res_cd") String resCd,
        @JsonProperty("res_msg") String resMsg,
        @JsonProperty("pay_method") String payMethod,
        @JsonProperty("tno") String tno,
        @JsonProperty("amount") String amount,
        @JsonProperty("card_cd") String cardCd,
        @JsonProperty("card_mny") String cardMny,
        @JsonProperty("card_name") String cardName,
        @JsonProperty("card_no") String cardNo,
        @JsonProperty("app_no") String appNo,
        @JsonProperty("app_time") String appTime,
        @JsonProperty("quota") String quota,
        @JsonProperty("acqu_cd") String acquCd,
        @JsonProperty("acqu_name") String acquName,
        @JsonProperty("partcanc_yn") String partcancYn,
        @JsonProperty("card_bin_type_01") String cardBinType01,
        @JsonProperty("card_bin_type_02") String cardBinType02
) {
    public boolean isSuccess() {
        return "0000".equals(resCd);
    }
}
