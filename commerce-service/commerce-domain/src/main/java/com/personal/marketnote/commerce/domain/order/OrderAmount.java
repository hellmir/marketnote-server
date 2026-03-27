package com.personal.marketnote.commerce.domain.order;

import lombok.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class OrderAmount {
    private Long totalAmount;
    private Long paidAmount;
    private Long couponAmount;
    private Long pointAmount;
    private Long shippingFee;

    @Deprecated
    public static OrderAmount of(
            Long totalAmount,
            Long paidAmount,
            Long couponAmount,
            Long pointAmount,
            Long shippingFee
    ) {
        return OrderAmount.builder()
                .totalAmount(totalAmount)
                .paidAmount(paidAmount)
                .couponAmount(couponAmount)
                .pointAmount(pointAmount)
                .shippingFee(shippingFee)
                .build();
    }

    public static OrderAmount from(OrderAmountCreateState state) {
        return OrderAmount.builder()
                .totalAmount(state.getTotalAmount())
                .couponAmount(state.getCouponAmount())
                .pointAmount(state.getPointAmount())
                .shippingFee(state.getShippingFee())
                .build();
    }

    public static OrderAmount from(OrderAmountSnapshotState state) {
        return OrderAmount.builder()
                .totalAmount(state.getTotalAmount())
                .paidAmount(state.getPaidAmount())
                .couponAmount(state.getCouponAmount())
                .pointAmount(state.getPointAmount())
                .shippingFee(state.getShippingFee())
                .build();
    }
}
