package com.personal.marketnote.notification.adapter.in.web.device.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.personal.marketnote.notification.port.in.result.device.RegisterDeviceTokenResult;
import io.swagger.v3.oas.annotations.media.Schema;

public record RegisterDeviceTokenResponse(
        @Schema(description = "디바이스 토큰 ID", example = "1")
        Long id,

        @JsonProperty("isNew")
        @Schema(description = "신규 등록 여부 (true: 신규, false: 기존 토큰 갱신)", example = "true")
        boolean isNew
) {

    public static RegisterDeviceTokenResponse from(RegisterDeviceTokenResult result) {
        return new RegisterDeviceTokenResponse(result.id(), result.isNew());
    }
}
