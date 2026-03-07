package com.personal.marketnote.reward.adapter.in.web.point.request;

import com.personal.marketnote.reward.domain.point.UserPointChangeType;
import com.personal.marketnote.reward.domain.point.UserPointSourceType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ModifyPendingPointRequest {
    @NotNull
    private UserPointChangeType changeType;

    @NotNull
    @Min(1)
    private Long amount;

    @NotNull
    private UserPointSourceType sourceType;

    @NotNull
    private Long sourceId;

    private String reason;
}
