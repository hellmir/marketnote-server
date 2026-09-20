package com.personal.marketnote.user.adapter.in.web.user.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateUserPenaltyCountRequest(
        @NotNull(message = "패널티 횟수는 필수입니다.")
        @Min(value = 0, message = "패널티 횟수는 0 이상이어야 합니다.")
        Integer penaltyCount,

        @NotBlank(message = "패널티 사유는 필수입니다.")
        @Size(max = 500, message = "패널티 사유는 500자 이하여야 합니다.")
        String reason
) {
}
