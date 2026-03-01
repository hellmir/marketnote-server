package com.personal.marketnote.commerce.adapter.in.web.settlement.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExecuteSettlementRequest {
    @NotNull(message = "정산 연도는 필수입니다")
    @Min(value = 2020, message = "연도는 2020 이상이어야 합니다")
    @Max(value = 2100, message = "연도는 2100 이하여야 합니다")
    private Integer year;

    @NotNull(message = "정산 월은 필수입니다")
    @Min(value = 1, message = "월은 1 이상이어야 합니다")
    @Max(value = 12, message = "월은 12 이하여야 합니다")
    private Integer month;

    @NotNull(message = "PG 수수료율은 필수입니다")
    @Min(value = 0, message = "PG 수수료율은 0 이상이어야 합니다")
    @Max(value = 10000, message = "PG 수수료율은 10000(100%) 이하여야 합니다")
    private Integer pgFeeRate;

    @NotNull(message = "플랫폼 수수료율은 필수입니다")
    @Min(value = 0, message = "플랫폼 수수료율은 0 이상이어야 합니다")
    @Max(value = 10000, message = "플랫폼 수수료율은 10000(100%) 이하여야 합니다")
    private Integer platformFeeRate;
}
