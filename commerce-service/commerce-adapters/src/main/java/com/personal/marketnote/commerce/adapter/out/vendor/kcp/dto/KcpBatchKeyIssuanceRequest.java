package com.personal.marketnote.commerce.adapter.out.vendor.kcp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record KcpBatchKeyIssuanceRequest(
        @JsonProperty("site_cd") String siteCd,
        @JsonProperty("kcp_cert_info") String kcpCertInfo,
        @JsonProperty("enc_data") String encData,
        @JsonProperty("enc_info") String encInfo,
        @JsonProperty("tran_cd") String tranCd
) {
}
