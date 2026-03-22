package com.personal.marketnote.commerce.adapter.in.web.order.request;

import com.personal.marketnote.common.utility.RegularExpressionConstant;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.util.List;

@Getter
public class RegisterOrderRequest {
    @Schema(
            name = "totalAmount",
            description = "총 주문 금액(원)",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "총 주문 금액은 필수값입니다.")
    @Min(value = 0, message = "총 주문 금액은 0 이상이어야 합니다.")
    private Long totalAmount;

    @Schema(
            name = "couponAmount",
            description = "쿠폰 할인 금액(원)",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Min(value = 0, message = "쿠폰 할인 금액은 0 이상이어야 합니다.")
    private Long couponAmount;

    @Schema(
            name = "pointAmount",
            description = "포인트 사용 금액(원)",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Min(value = 0, message = "포인트 사용 금액은 0 이상이어야 합니다.")
    private Long pointAmount;

    @Schema(
            name = "shippingFee",
            description = "배송비(원)",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Min(value = 0, message = "배송비는 0 이상이어야 합니다.")
    @Max(value = 1000000, message = "배송비는 1,000,000원을 초과할 수 없습니다.")
    private Long shippingFee;

    @Schema(
            name = "recipientName",
            description = "수령인명",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "수령인명은 필수값입니다.")
    @Size(max = 50, message = "수령인명은 50자를 초과할 수 없습니다.")
    @Pattern(regexp = RegularExpressionConstant.RECIPIENT_NAME_PATTERN, message = "수령인명 형식이 올바르지 않습니다.")
    private String recipientName;

    @Schema(
            name = "recipientPhoneNumber",
            description = "수령인 전화번호",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "수령인 전화번호는 필수값입니다.")
    @Size(max = 20, message = "수령인 전화번호는 20자를 초과할 수 없습니다.")
    @Pattern(regexp = RegularExpressionConstant.PHONE_NUMBER_PATTERN, message = "전화번호 형식이 올바르지 않습니다.")
    private String recipientPhoneNumber;

    @Schema(
            name = "zipCode",
            description = "우편번호",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "우편번호는 필수값입니다.")
    @Pattern(regexp = RegularExpressionConstant.ZIP_CODE_PATTERN, message = "우편번호는 5자리 숫자여야 합니다.")
    private String zipCode;

    @Schema(
            name = "address",
            description = "주소",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "주소는 필수값입니다.")
    @Size(max = 255, message = "주소는 255자를 초과할 수 없습니다.")
    @Pattern(regexp = RegularExpressionConstant.NO_HTML_TAG_PATTERN, message = "주소에 허용되지 않는 문자가 포함되어 있습니다.")
    private String address;

    @Schema(
            name = "addressDetail",
            description = "상세주소",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Size(max = 255, message = "상세주소는 255자를 초과할 수 없습니다.")
    @Pattern(regexp = RegularExpressionConstant.NO_HTML_TAG_PATTERN, message = "상세주소에 허용되지 않는 문자가 포함되어 있습니다.")
    private String addressDetail;

    @Schema(
            name = "requestMessage",
            description = "배송 요청사항",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Size(max = 100, message = "배송 요청사항은 100자를 초과할 수 없습니다.")
    @Pattern(regexp = RegularExpressionConstant.NO_HTML_TAG_PATTERN, message = "배송 요청사항에 허용되지 않는 문자가 포함되어 있습니다.")
    private String requestMessage;

    @Schema(
            name = "orderProducts",
            description = "주문 상품 목록",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotEmpty(message = "주문 상품 목록은 필수값입니다.")
    @Valid
    private List<OrderProductItemRequest> orderProducts;
}
