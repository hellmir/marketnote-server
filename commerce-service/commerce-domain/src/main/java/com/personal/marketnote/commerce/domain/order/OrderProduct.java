package com.personal.marketnote.commerce.domain.order;

import com.personal.marketnote.common.utility.FormatValidator;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class OrderProduct {
    private static final long REVIEW_DEADLINE_DAYS = 30;

    private Long orderId;
    private Long sellerId;
    private Long pricePolicyId;
    private Long sharerId;
    private Integer quantity;
    private Long unitAmount;
    private String imageUrl;
    private OrderStatus orderStatus;
    private Boolean isReviewed;
    private LocalDateTime confirmedAt;

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
                .confirmedAt(state.getConfirmedAt())
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
