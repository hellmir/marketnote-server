package com.personal.marketnote.common.adapter.in.web.kafka.request;

import com.personal.marketnote.common.configuration.kafka.DltResolutionAction;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ResolveDltRequest(
        @NotBlank(message = "원본 토픽명은 필수입니다")
        String originalTopic,

        @NotNull(message = "파티션 번호는 필수입니다")
        @Min(value = 0, message = "파티션 번호는 0 이상이어야 합니다")
        Integer partition,

        @NotNull(message = "오프셋 번호는 필수입니다")
        @Min(value = 0, message = "오프셋 번호는 0 이상이어야 합니다")
        Long offset,

        @NotNull(message = "처리 액션은 필수입니다")
        DltResolutionAction action,

        @Size(max = 500, message = "처리 사유는 500자를 초과할 수 없습니다")
        String reason
) {
}
