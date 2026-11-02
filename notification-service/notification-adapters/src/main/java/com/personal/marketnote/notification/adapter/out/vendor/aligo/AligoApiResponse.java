package com.personal.marketnote.notification.adapter.out.vendor.aligo;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

public record AligoApiResponse(
        @JsonProperty("result_code") @JsonAlias("code") int resultCode,
        String message
) {
}
