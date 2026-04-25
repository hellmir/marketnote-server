package com.personal.marketnote.commerce.domain.order;

import com.personal.marketnote.common.utility.FormatValidator;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class OrderProduct {
    private static final long REVIEW_DEADLINE_DAYS = 30;

    private Long orderId;
    private Long sellerId;
    private Long pricePolicyId;
    private UUID sharerKey;
    private Integer quantity;
    private Long unitAmount;
    private String imageUrl;
    private Long accumulatedPoint;
    private OrderStatus orderStatus;
    private Boolean isReviewed;
    private LocalDateTime confirmedAt;
    private LocalDateTime deliveredAt;

    public static OrderProduct from(OrderProductCreateState state) {
        return OrderProduct.builder()
                .sellerId(state.getSellerId())
                .pricePolicyId(state.getPricePolicyId())
                .sharerKey(state.getSharerKey())
                .quantity(state.getQuantity())
                .unitAmount(state.getUnitAmount())
                .imageUrl(state.getImageUrl())
                .accumulatedPoint(state.getAccumulatedPoint())
                .orderStatus(OrderStatus.PAYMENT_PENDING)
                .build();
    }

    public static OrderProduct from(OrderProductSnapshotState state) {
        return OrderProduct.builder()
                .orderId(state.getOrderId())
                .sellerId(state.getSellerId())
                .pricePolicyId(state.getPricePolicyId())
                .sharerKey(state.getSharerKey())
                .quantity(state.getQuantity())
                .unitAmount(state.getUnitAmount())
                .imageUrl(state.getImageUrl())
                .accumulatedPoint(state.getAccumulatedPoint())
                .orderStatus(state.getOrderStatus())
                .isReviewed(state.getIsReviewed())
                .confirmedAt(state.getConfirmedAt())
                .deliveredAt(state.getDeliveredAt())
                .build();
    }

    public void changeOrderStatus(OrderStatus orderStatus, LocalDateTime now) {
        if (this.orderStatus == orderStatus) {
            return;
        }
        if (!this.orderStatus.canTransitionTo(orderStatus)) {
            throw new InvalidOrderProductStatusTransitionException(this.orderStatus, orderStatus);
        }
        this.orderStatus = orderStatus;
        if (orderStatus.isDelivered()) {
            this.deliveredAt = now;
        }
        if (orderStatus.isConfirmed()) {
            this.confirmedAt = now;
        }
    }

    public boolean isConfirmed() {
        return FormatValidator.hasValue(this.orderStatus) && this.orderStatus.isConfirmed();
    }

    public boolean isWithinReviewDeadline(LocalDateTime now) {
        // confirmedAt이 null인 기존 데이터는 기한 내로 간주 (마이그레이션 전 데이터 호환)
        if (FormatValidator.hasNoValue(confirmedAt)) {
            return true;
        }
        LocalDateTime deadline = calculateReviewDeadline();
        return !deadline.isBefore(now);
    }

    public LocalDateTime calculateReviewDeadline() {
        if (FormatValidator.hasNoValue(confirmedAt)) {
            return null;
        }
        return confirmedAt.plusDays(REVIEW_DEADLINE_DAYS);
    }

    public void updateReviewStatus(Boolean isReviewed) {
        this.isReviewed = isReviewed;
    }
}
