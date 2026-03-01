package com.personal.marketnote.commerce.domain.order;

import lombok.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class OrderProduct {
    private Long orderId;
    private Long sellerId;
    private Long pricePolicyId;
    private Long sharerId;
    private Integer quantity;
    private Long unitAmount;
    private String imageUrl;
    private OrderStatus orderStatus;
    private Boolean isReviewed;

    public static OrderProduct from(OrderProductCreateState state) {
        return OrderProduct.builder()
                .sellerId(state.getSellerId())
                .pricePolicyId(state.getPricePolicyId())
                .sharerId(state.getSharerId())
                .quantity(state.getQuantity())
                .unitAmount(state.getUnitAmount())
                .imageUrl(state.getImageUrl())
                .orderStatus(OrderStatus.PAYMENT_PENDING)
                .build();
    }

    public static OrderProduct from(OrderProductSnapshotState state) {
        return OrderProduct.builder()
                .orderId(state.getOrderId())
                .sellerId(state.getSellerId())
                .pricePolicyId(state.getPricePolicyId())
                .sharerId(state.getSharerId())
                .quantity(state.getQuantity())
                .unitAmount(state.getUnitAmount())
                .imageUrl(state.getImageUrl())
                .orderStatus(state.getOrderStatus())
                .isReviewed(state.getIsReviewed())
                .build();
    }

    public void changeOrderStatus(OrderStatus orderStatus) {
        if (this.orderStatus == orderStatus) {
            return;
        }
        if (!this.orderStatus.canTransitionTo(orderStatus)) {
            throw new InvalidOrderProductStatusTransitionException(this.orderStatus, orderStatus);
        }
        this.orderStatus = orderStatus;
    }

    public void updateReviewStatus(Boolean isReviewed) {
        this.isReviewed = isReviewed;
    }
}
