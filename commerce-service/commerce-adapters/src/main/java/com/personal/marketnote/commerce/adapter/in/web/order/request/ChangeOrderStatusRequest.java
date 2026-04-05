package com.personal.marketnote.commerce.adapter.in.web.order.request;

import com.personal.marketnote.commerce.domain.order.OrderStatus;
import com.personal.marketnote.commerce.domain.order.OrderStatusReasonCategory;
import com.personal.marketnote.common.domain.delivery.PickupRequestType;
import com.personal.marketnote.common.utility.RegularExpressionConstant;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.util.List;

@Getter
public class ChangeOrderStatusRequest {
    @Schema(
            name = "pricePolicyIds",
            description = "가격 정책 ID 목록",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private List<Long> pricePolicyIds;

    @NotNull(message = "주문 상태는 필수값입니다.")
    @Schema(
            name = "orderStatus",
            description = "주문 상태",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private OrderStatus orderStatus;

    @Schema(
            name = "reasonCategory",
            description = "변경 사유 카테고리",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private OrderStatusReasonCategory reasonCategory;

    @Schema(
            name = "reason",
            description = "변경 사유",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Size(max = 500, message = "변경 사유는 500자 이내여야 합니다.")
    private String reason;

    @Schema(
            name = "pickupAddressId",
            description = "회수지 배송지 ID (반품 신청 시 사용, 미입력 시 배송지 기본값)",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private Long pickupAddressId;

    @Schema(
            name = "pickupRequestType",
            description = "회수 요청 타입 (반품 신청 시 사용)",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private PickupRequestType pickupRequestType;

    @Schema(
            name = "pickupRequestMessage",
            description = "회수 요청사항 (반품 신청 시 사용, pickupRequestType이 CUSTOM인 경우 필수, 60자 제한)",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Size(max = 60, message = "회수 요청사항은 60자 이내여야 합니다.")
    @Pattern(regexp = RegularExpressionConstant.NO_HTML_TAG_PATTERN, message = "회수 요청사항에 허용되지 않는 문자가 포함되어 있습니다.")
    private String pickupRequestMessage;
}
