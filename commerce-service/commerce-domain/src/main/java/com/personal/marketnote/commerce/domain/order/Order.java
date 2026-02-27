package com.personal.marketnote.commerce.domain.order;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.common.utility.RandomCodeGenerator;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class Order {
    private Long id;
    private Long buyerId;
    private UUID orderKey;
    private String orderNumber;
    private OrderStatus orderStatus;
    private OrderStatusReasonCategory statusChangeReasonCategory;
    private String statusChangeReason;
    private Long totalAmount;
    private Long paidAmount;
    private Long couponAmount;
    private Long pointAmount;
    private List<OrderProduct> orderProducts;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    public static Order from(OrderCreateState state) {
        List<OrderProduct> orderProducts = FormatValidator.hasValue(state.getOrderProductStates())
                ? state.getOrderProductStates().stream()
                .map(OrderProduct::from)
                .toList()
                : List.of();

        return Order.builder()
                .buyerId(state.getBuyerId())
                .orderKey(RandomCodeGenerator.generateOrderKey())
                .orderNumber(RandomCodeGenerator.generateOrderNumber())
                .orderStatus(OrderStatus.PAYMENT_PENDING)
                .totalAmount(state.getTotalAmount())
                .couponAmount(state.getCouponAmount())
                .pointAmount(state.getPointAmount())
                .orderProducts(orderProducts)
                .build();
    }

    public static Order from(OrderSnapshotState state) {
        List<OrderProduct> orderProducts = FormatValidator.hasValue(state.getOrderProductStates())
                ? state.getOrderProductStates().stream()
                .map(OrderProduct::from)
                .toList()
                : List.of();

        return Order.builder()
                .id(state.getId())
                .buyerId(state.getBuyerId())
                .orderKey(state.getOrderKey())
                .orderNumber(state.getOrderNumber())
                .orderStatus(state.getOrderStatus())
                .statusChangeReasonCategory(state.getStatusChangeReasonCategory())
                .statusChangeReason(state.getStatusChangeReason())
                .totalAmount(state.getTotalAmount())
                .paidAmount(state.getPaidAmount())
                .couponAmount(state.getCouponAmount())
                .pointAmount(state.getPointAmount())
                .orderProducts(orderProducts)
                .createdAt(state.getCreatedAt())
                .modifiedAt(state.getModifiedAt())
                .build();
    }

    public boolean isPaymentPending() {
        return this.orderStatus.isPending();
    }

    public void changeProductsStatus(List<Long> pricePolicyIds, OrderStatus orderStatus) {
        orderProducts.stream()
                .filter(orderProduct -> pricePolicyIds.contains(orderProduct.getPricePolicyId()))
                .forEach(orderProduct -> orderProduct.changeOrderStatus(orderStatus));

        if (
                orderStatus.isRefunded()
                        && orderProducts.stream()
                        .anyMatch(orderProduct -> !orderProduct.getOrderStatus().isRefunded())
        ) {
            this.orderStatus = OrderStatus.getPartiallyRefunded();
            return;
        }

        if (
                orderStatus.isConfirmed()
                        && orderProducts.stream()
                        .anyMatch(orderProduct -> !orderProduct.getOrderStatus().isConfirmed())
        ) {
            this.orderStatus = OrderStatus.getPartiallyConfirmed();
            return;
        }

        this.orderStatus = orderStatus;
    }

    public void changeAllProductsStatus(OrderStatus orderStatus) {
        orderProducts.forEach(orderProduct -> orderProduct.changeOrderStatus(orderStatus));
        this.orderStatus = orderStatus;
    }
}
