package com.personal.marketnote.fulfillment.adapter.in.web.delivery.request;

import jakarta.validation.constraints.NotNull;

public record CancelInternalFulfillmentDeliveryRequest(
        @NotNull(message = "주문 ID는 필수입니다")
        Long orderId
) {
}
