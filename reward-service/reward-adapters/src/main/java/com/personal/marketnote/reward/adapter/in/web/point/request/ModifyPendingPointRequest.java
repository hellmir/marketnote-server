package com.personal.marketnote.reward.adapter.in.web.point.request;

import com.personal.marketnote.reward.domain.point.UserPointChangeType;
import com.personal.marketnote.reward.domain.point.UserPointSourceType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ModifyPendingPointRequest(
        @NotNull
        UserPointChangeType changeType,

        @NotNull
        @Min(1)
        Long amount,

        @NotNull
        UserPointSourceType sourceType,

        @NotNull
        Long sourceId,

        String reason
) {
}
