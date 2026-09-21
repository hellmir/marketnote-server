package com.personal.marketnote.user.adapter.in.web.user.request;

import com.personal.marketnote.user.domain.user.UserStatusAction;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record ChangeUserStatusRequest(
        @NotNull(message = "상태 변경 액션은 필수입니다.")
        UserStatusAction action,

        @NotBlank(message = "상태 변경 사유는 필수입니다.")
        @Size(max = 500, message = "상태 변경 사유는 500자 이하여야 합니다.")
        String reason,

        @Future(message = "비활성화 종료 일시는 현재보다 미래여야 합니다.")
        LocalDateTime deactivatedUntil
) {
}
