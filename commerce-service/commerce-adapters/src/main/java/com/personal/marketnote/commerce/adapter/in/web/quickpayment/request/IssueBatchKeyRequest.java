package com.personal.marketnote.commerce.adapter.in.web.quickpayment.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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

    @Schema(
            name = "cardMaskNo",
            description = "KCP 결제창 인증결과 마스킹 카드번호 (예: 123412******1234)",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Size(max = 20, message = "cardMaskNo는 20자를 초과할 수 없습니다.")
    @Pattern(
            regexp = "^[0-9*\\-]{12,20}$",
            message = "cardMaskNo 형식이 올바르지 않습니다. 12~20자 숫자/별표/하이픈만 허용됩니다."
    )
    private String cardMaskNo;
}
