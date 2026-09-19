package com.personal.marketnote.commerce.domain.order;

import com.personal.marketnote.common.domain.money.Money;
import com.personal.marketnote.common.utility.FormatValidator;
import lombok.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class OrderAmount {
    private Money totalAmount;
    private Long paidAmount;
    private Money couponAmount;
    private Money pointAmount;
    private Money shippingFee;

    @Deprecated
    public static OrderAmount of(
            Long totalAmount,
            Long paidAmount,
            Long couponAmount,
            Long pointAmount,
            Long shippingFee
    ) {
        return OrderAmount.builder()
                .totalAmount(resolveMoneyOrZero(totalAmount))
                .paidAmount(paidAmount)
                .couponAmount(resolveMoneyOrZero(couponAmount))
                .pointAmount(resolveMoneyOrZero(pointAmount))
                .shippingFee(resolveMoneyOrZero(shippingFee))
                .build();
    }

    public static OrderAmount from(OrderAmountCreateState state) {
        return OrderAmount.builder()
                .totalAmount(Money.of(state.getTotalAmount()))
                .couponAmount(resolveMoneyOrZero(state.getCouponAmount()))
                .pointAmount(resolveMoneyOrZero(state.getPointAmount()))
                .shippingFee(resolveMoneyOrZero(state.getShippingFee()))
                .build();
    }

    public static OrderAmount from(OrderAmountSnapshotState state) {
        return OrderAmount.builder()
                .totalAmount(resolveMoneyOrZero(state.getTotalAmount()))
                .paidAmount(state.getPaidAmount())
                .couponAmount(resolveMoneyOrZero(state.getCouponAmount()))
                .pointAmount(resolveMoneyOrZero(state.getPointAmount()))
                .shippingFee(resolveMoneyOrZero(state.getShippingFee()))
                .build();
    }

    private static Money resolveMoneyOrZero(Long value) {
        if (FormatValidator.hasNoValue(value)) {
            return Money.zero();
        }
        return Money.of(value);
    }
}
