package com.personal.marketnote.community.adapter.in.web.like.request;

import com.personal.marketnote.community.domain.like.LikeTargetType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record UpsertLikeRequest(
        @Schema(description = "대상 유형(REVIEW/BOARD)", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "대상 유형은 필수입니다.")
        LikeTargetType targetType,

        @Schema(description = "대상 ID", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "대상 ID는 필수입니다.")
        Long targetId,

        @Schema(description = "좋아요 여부(좋아요/취소)", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "좋아요 여부는 필수입니다.")
        boolean isLiked
) {
}
