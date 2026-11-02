package com.personal.marketnote.commerce.adapter.in.web.payment.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ResolveUnknownPaymentRequest(
        @NotBlank(message = "해소 상태는 필수입니다 (COMPLETE 또는 FAILED)")
        @Pattern(regexp = "^(COMPLETE|FAILED)$", message = "해소 상태는 COMPLETE 또는 FAILED만 허용됩니다")
        String resolvedStatus,
        String resultCode,
        String resultMessage,
        String pgPaymentKey,
        String approvalNumber,
        String appTime
) {
}
