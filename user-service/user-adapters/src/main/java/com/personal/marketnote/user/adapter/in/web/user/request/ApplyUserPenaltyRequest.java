package com.personal.marketnote.user.adapter.in.web.user.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ApplyUserPenaltyRequest(
        @NotBlank(message = "패널티 사유는 필수입니다.")
        @Size(max = 500, message = "패널티 사유는 500자 이하여야 합니다.")
        String reason
) {
}
