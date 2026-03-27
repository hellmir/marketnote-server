package com.personal.marketnote.commerce.adapter.in.web.order.request;

import com.personal.marketnote.common.utility.RegularExpressionConstant;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
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
            name = "shippingAddressId",
            description = "배송지 ID",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "배송지 ID는 필수값입니다.")
    private Long shippingAddressId;

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
