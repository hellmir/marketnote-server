package com.personal.marketnote.commerce.adapter.in.web.order.request;

import com.personal.marketnote.commerce.domain.order.OrderStatusReasonCategory;
import com.personal.marketnote.common.utility.RegularExpressionConstant;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class RequestRefundRequest {
    @Schema(
            name = "reasonCategory",
            description = "환불 사유 카테고리",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private OrderStatusReasonCategory reasonCategory;

    @Schema(
            name = "reason",
            description = "환불 사유",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Size(max = 500, message = "환불 사유는 500자 이내여야 합니다.")
    private String reason;

    @Schema(
            name = "pickupRecipientName",
            description = "회수지 수령인명 (미입력 시 배송지 기본값)",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Size(max = 50, message = "회수지 수령인명은 50자 이내여야 합니다.")
    @Pattern(regexp = RegularExpressionConstant.RECIPIENT_NAME_PATTERN, message = "회수지 수령인명 형식이 올바르지 않습니다.")
    private String pickupRecipientName;

    @Schema(
            name = "pickupRecipientPhoneNumber",
            description = "회수지 연락처 (미입력 시 배송지 기본값)",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Size(max = 20, message = "회수지 연락처는 20자 이내여야 합니다.")
    @Pattern(regexp = RegularExpressionConstant.PHONE_NUMBER_PATTERN, message = "회수지 연락처 형식이 올바르지 않습니다.")
    private String pickupRecipientPhoneNumber;

    @Schema(
            name = "pickupZipCode",
            description = "회수지 우편번호 (미입력 시 배송지 기본값)",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Pattern(regexp = RegularExpressionConstant.ZIP_CODE_PATTERN, message = "회수지 우편번호는 5자리 숫자여야 합니다.")
    private String pickupZipCode;

    @Schema(
            name = "pickupAddress",
            description = "회수지 주소 (미입력 시 배송지 기본값)",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Size(max = 255, message = "회수지 주소는 255자 이내여야 합니다.")
    @Pattern(regexp = RegularExpressionConstant.NO_HTML_TAG_PATTERN, message = "회수지 주소에 허용되지 않는 문자가 포함되어 있습니다.")
    private String pickupAddress;

    @Schema(
            name = "pickupAddressDetail",
            description = "회수지 상세주소 (미입력 시 배송지 기본값)",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Size(max = 255, message = "회수지 상세주소는 255자 이내여야 합니다.")
    @Pattern(regexp = RegularExpressionConstant.NO_HTML_TAG_PATTERN, message = "회수지 상세주소에 허용되지 않는 문자가 포함되어 있습니다.")
    private String pickupAddressDetail;

    @Schema(
            name = "pickupRequestMessage",
            description = "회수 요청사항 (60자 제한)",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Size(max = 60, message = "회수 요청사항은 60자 이내여야 합니다.")
    @Pattern(regexp = RegularExpressionConstant.NO_HTML_TAG_PATTERN, message = "회수 요청사항에 허용되지 않는 문자가 포함되어 있습니다.")
    private String pickupRequestMessage;
}
