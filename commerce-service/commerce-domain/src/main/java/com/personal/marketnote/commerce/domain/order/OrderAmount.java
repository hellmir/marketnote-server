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
}
