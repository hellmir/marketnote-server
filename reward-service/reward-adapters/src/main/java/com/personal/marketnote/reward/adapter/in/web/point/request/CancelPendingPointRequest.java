package com.personal.marketnote.reward.adapter.in.web.point.request;

import com.personal.marketnote.reward.domain.point.UserPointSourceType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CancelPendingPointRequest {
    @NotNull
    private UserPointSourceType sourceType;

    @NotNull
    @Min(1)
    private Long sourceId;

    @Size(max = 500)
    private String reason;
}
