package com.personal.marketnote.community.adapter.in.web.report.request;

import com.personal.marketnote.community.domain.report.ReportTargetType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record UpdateTargetStatusRequest(
        @Schema(description = "대상 유형(REVIEW/POST)", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "대상 유형은 필수입니다.")
        ReportTargetType targetType,

        @Schema(description = "대상 ID", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "대상 ID는 필수입니다.")
        Long targetId,

        @Schema(description = "노출 여부(노출/숨기기)", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "노출 여부는 필수입니다.")
        boolean isVisible
) {
}
