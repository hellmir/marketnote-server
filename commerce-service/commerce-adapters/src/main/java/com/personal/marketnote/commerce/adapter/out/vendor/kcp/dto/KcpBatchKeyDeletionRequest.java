package com.personal.marketnote.commerce.adapter.out.vendor.kcp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record KcpBatchKeyDeletionRequest(
        @JsonProperty("kcp_cert_info") String kcpCertInfo,
        @JsonProperty("site_cd") String siteCd,
        @JsonProperty("pay_method") String payMethod,
        @JsonProperty("tx_type") String txType,
        @JsonProperty("group_id") String groupId,
        @JsonProperty("batch_key") String batchKey
) {
}
