package com.personal.marketnote.commerce.adapter.out.vendor.kcp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record KcpBatchPaymentApprovalRequest(
        @JsonProperty("kcp_cert_info") String kcpCertInfo,
        @JsonProperty("site_cd") String siteCd,
        @JsonProperty("pay_method") String payMethod,
        @JsonProperty("amount") String amount,
        @JsonProperty("card_mny") String cardMny,
        @JsonProperty("quota") String quota,
        @JsonProperty("currency") String currency,
        @JsonProperty("ordr_idxx") String ordrIdxx,
        @JsonProperty("good_name") String goodName,
        @JsonProperty("card_tx_type") String cardTxType,
        @JsonProperty("bt_batch_key") String btBatchKey,
        @JsonProperty("bt_group_id") String btGroupId,
        @JsonProperty("media_type") String mediaType
) {
}
