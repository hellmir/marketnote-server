package com.personal.marketnote.commerce.adapter.out.vendor.kcp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record KcpPaymentCancelRequest(
        @JsonProperty("site_cd") String siteCd,
        @JsonProperty("tno") String tno,
        @JsonProperty("kcp_cert_info") String kcpCertInfo,
        @JsonProperty("kcp_sign_data") String kcpSignData,
        @JsonProperty("mod_type") String modType,
        @JsonProperty("mod_mny") String modMny,
        @JsonProperty("rem_mny") String remMny,
        @JsonProperty("mod_desc") String modDesc
) {
}
