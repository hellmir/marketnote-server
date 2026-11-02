package com.personal.marketnote.common.adapter.in.web.kafka.request;

import jakarta.validation.constraints.NotBlank;

public record ReprocessDltRequest(
        @NotBlank(message = "원본 토픽명은 필수입니다")
        String originalTopic
) {
}
