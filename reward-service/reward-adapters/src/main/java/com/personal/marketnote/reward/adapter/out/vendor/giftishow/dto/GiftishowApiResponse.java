package com.personal.marketnote.reward.adapter.out.vendor.giftishow.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GiftishowApiResponse<T>(
        @JsonProperty("code") String code,
        @JsonProperty("message") String message,
        @JsonProperty("result") T result
) {
    public boolean isSuccess() {
        return "0000".equals(code);
    }
}
