package com.personal.marketnote.commerce.port.in.result.order;

import com.personal.marketnote.commerce.domain.order.Order;
import com.personal.marketnote.commerce.domain.order.OrderStatus;
import com.personal.marketnote.commerce.domain.order.OrderStatusReasonCategory;
import com.personal.marketnote.commerce.port.out.result.product.ProductInfoResult;
import lombok.AccessLevel;
import lombok.Builder;

import java.util.List;
import java.util.Map;

@Builder(access = AccessLevel.PRIVATE)
public record GetOrderResult(
        Long id,
        Long buyerId,
        String orderNumber,
        OrderStatus orderStatus,
        OrderStatusReasonCategory statusChangeReasonCategory,
        String statusChangeReason,
        Long totalAmount,
        Long paidAmount,
        Long couponAmount,
        Long pointAmount,
        List<GetOrderProductResult> orderProducts
) {
    public static GetOrderResult from(
            Order order,
            Map<Long, ProductInfoResult> productInfoResultsByPricePolicyId
    ) {
        return GetOrderResult.builder()
                .id(order.getId())
                .buyerId(order.getBuyerId())
                .orderNumber(order.getOrderNumber())
                .orderStatus(order.getOrderStatus())
                .statusChangeReasonCategory(order.getStatusChangeReasonCategory())
                .statusChangeReason(order.getStatusChangeReason())
                .totalAmount(order.getTotalAmount())
                .paidAmount(order.getPaidAmount())
                .couponAmount(order.getCouponAmount())
                .pointAmount(order.getPointAmount())
                .orderProducts(order.getOrderProducts().stream()
                        .map(orderProduct -> GetOrderProductResult.from(
                                        orderProduct,
                                        productInfoResultsByPricePolicyId.get(orderProduct.getPricePolicyId()),
                                        order.getOrderStatus()
                                )
                        )
                        .toList())
                .build();
    }
}
