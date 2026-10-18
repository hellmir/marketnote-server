package com.personal.marketnote.fulfillment.adapter.in.web.delivery.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CancelInternalFulfillmentDeliveryRequest(
        @NotNull(message = "주문 ID는 필수입니다")
        Long orderId,
        List<@NotBlank(message = "productKey는 공백일 수 없습니다.") String> productKeys
) {
}
