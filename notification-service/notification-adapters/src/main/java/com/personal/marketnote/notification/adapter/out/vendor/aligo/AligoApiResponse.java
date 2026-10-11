package com.personal.marketnote.notification.adapter.out.vendor.aligo;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AligoApiResponse(
        @JsonProperty("result_code") int resultCode,
        String message
) {
}
