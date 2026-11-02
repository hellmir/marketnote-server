package com.personal.marketnote.product.adapter.in.web.cart.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record GetMyOrderingProductsRequest(
        @Schema(description = "주문 대기 상품 목록")
        @NotNull(message = "주문 대기 상품 목록은 필수입니다.")
        List<OrderingItemRequest> orderingItemRequests
) {
}
