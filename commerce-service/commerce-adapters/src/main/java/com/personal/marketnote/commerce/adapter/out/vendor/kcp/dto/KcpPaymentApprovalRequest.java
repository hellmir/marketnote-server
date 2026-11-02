package com.personal.marketnote.commerce.adapter.out.vendor.kcp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record KcpPaymentApprovalRequest(
        @JsonProperty("site_cd") String siteCd,
        @JsonProperty("enc_data") String encData,
        @JsonProperty("enc_info") String encInfo,
        @JsonProperty("tran_cd") String tranCd,
        @JsonProperty("kcp_cert_info") String kcpCertInfo,
        @JsonProperty("ordr_mony") String ordrMony,
        @JsonProperty("ordr_no") String ordrNo,
        @JsonProperty("pay_type") String payType
) {
}
