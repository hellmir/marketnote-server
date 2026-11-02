package com.personal.marketnote.common.outbox.adapter.in.web.request;

import com.personal.marketnote.common.outbox.OutboxResolutionAction;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ResolveOutboxRequest(
        @NotNull(message = "이벤트 ID는 필수입니다")
        Long id,

        @NotNull(message = "처리 액션은 필수입니다")
        OutboxResolutionAction action,

        @Size(max = 500, message = "처리 사유는 500자를 초과할 수 없습니다")
        String reason
) {
}
