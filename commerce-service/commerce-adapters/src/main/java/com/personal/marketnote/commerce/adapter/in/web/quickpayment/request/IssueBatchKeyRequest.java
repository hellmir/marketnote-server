package com.personal.marketnote.commerce.adapter.in.web.quickpayment.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class IssueBatchKeyRequest {
    @Schema(
            name = "encData",
            description = "KCP 결제창 인증결과 암호화 데이터",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "enc_data는 필수값입니다.")
    @Size(max = 10000, message = "enc_data는 10000자를 초과할 수 없습니다.")
    private String encData;

    @Schema(
            name = "encInfo",
            description = "KCP 결제창 인증결과 암호화 정보",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "enc_info는 필수값입니다.")
    @Size(max = 10000, message = "enc_info는 10000자를 초과할 수 없습니다.")
    private String encInfo;
}
