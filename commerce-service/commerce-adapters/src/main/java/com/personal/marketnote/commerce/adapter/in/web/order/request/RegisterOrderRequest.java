package com.personal.marketnote.commerce.adapter.in.web.order.request;

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
            name = "recipientName",
            description = "수령인 이름",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "홍길동"
    )
    @NotBlank(message = "수령인 이름은 필수값입니다.")
    @Size(max = 31, message = "수령인 이름은 31자 이하여야 합니다.")
    private String recipientName;

    @Schema(
            name = "address",
            description = "배송지 주소",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "서울특별시 강남구 테헤란로 123"
    )
    @NotBlank(message = "배송지 주소는 필수값입니다.")
    @Size(max = 255, message = "배송지 주소는 255자 이하여야 합니다.")
    private String address;

    @Schema(
            name = "addressDetail",
            description = "배송지 상세 주소",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            example = "101동 1001호"
    )
    @Size(max = 255, message = "배송지 상세 주소는 255자 이하여야 합니다.")
    private String addressDetail;

    @Schema(
            name = "zipCode",
            description = "우편번호",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "06142"
    )
    @NotBlank(message = "우편번호는 필수값입니다.")
    @Size(max = 10, message = "우편번호는 10자 이하여야 합니다.")
    private String zipCode;

    @Schema(
            name = "phoneNumber",
            description = "수령인 연락처",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "010-1234-5678"
    )
    @NotBlank(message = "수령인 연락처는 필수값입니다.")
    @Size(max = 20, message = "수령인 연락처는 20자 이하여야 합니다.")
    private String phoneNumber;

    @Schema(
            name = "orderProducts",
            description = "주문 상품 목록",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotEmpty(message = "주문 상품 목록은 필수값입니다.")
    @Valid
    private List<OrderProductItemRequest> orderProducts;
}
