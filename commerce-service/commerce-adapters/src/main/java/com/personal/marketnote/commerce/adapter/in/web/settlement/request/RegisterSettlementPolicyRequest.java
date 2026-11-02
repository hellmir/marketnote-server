package com.personal.marketnote.commerce.adapter.in.web.settlement.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RegisterSettlementPolicyRequest {
    @NotNull(message = "판매자 ID는 필수입니다")
    @Min(value = 1, message = "판매자 ID는 1 이상이어야 합니다")
    private Long sellerId;

    @NotNull(message = "PG 수수료율은 필수입니다")
    @Min(value = 0, message = "PG 수수료율은 0 이상이어야 합니다")
    @Max(value = 10000, message = "PG 수수료율은 10000(100%) 이하여야 합니다")
    private Integer pgFeeRate;

    @NotNull(message = "플랫폼 수수료율은 필수입니다")
    @Min(value = 0, message = "플랫폼 수수료율은 0 이상이어야 합니다")
    @Max(value = 10000, message = "플랫폼 수수료율은 10000(100%) 이하여야 합니다")
    private Integer platformFeeRate;

    @NotBlank(message = "정산 주기는 필수입니다")
    private String settlementCycle;

    @NotNull(message = "최소 지급 금액은 필수입니다")
    @Min(value = 0, message = "최소 지급 금액은 0 이상이어야 합니다")
    private Long minPayoutAmount;
}
