package com.personal.marketnote.reward.adapter.in.web.point.request;

import com.personal.marketnote.reward.domain.point.UserPointSourceType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ConfirmPendingPointRequest(
        @NotNull
        UserPointSourceType sourceType,

        @NotNull
        @Min(1)
        Long sourceId,

        @Size(max = 500)
        String reason
) {
}
