package com.personal.marketnote.reward.adapter.in.web.point.request;

import com.personal.marketnote.reward.domain.point.UserPointChangeType;
import com.personal.marketnote.reward.domain.point.UserPointSourceType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ModifyUserPointRequest(
        @NotNull
        UserPointChangeType changeType,

        @NotNull
        @Min(0)
        Long amount,

        @NotNull
        UserPointSourceType sourceType,

        @NotNull
        Long sourceId,

        String reason
) {
}
