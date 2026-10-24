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
    private Money paidAmount;
    private Money couponAmount;
    private Money pointAmount;
    private Money shippingFee;

    public static OrderAmount from(OrderAmountCreateState state) {
        return OrderAmount.builder()
                .totalAmount(resolveMoneyOrZero(state.getTotalAmount()))
                .paidAmount(resolveMoneyOrZero(state.getPaidAmount()))
                .couponAmount(resolveMoneyOrZero(state.getCouponAmount()))
                .pointAmount(resolveMoneyOrZero(state.getPointAmount()))
                .shippingFee(resolveMoneyOrZero(state.getShippingFee()))
                .build();
    }

    public static OrderAmount from(OrderAmountSnapshotState state) {
        return OrderAmount.builder()
                .totalAmount(resolveMoneyOrZero(state.getTotalAmount()))
                .paidAmount(resolveMoneyOrZero(state.getPaidAmount()))
                .couponAmount(resolveMoneyOrZero(state.getCouponAmount()))
                .pointAmount(resolveMoneyOrZero(state.getPointAmount()))
                .shippingFee(resolveMoneyOrZero(state.getShippingFee()))
                .build();
    }

    public OrderAmount withPaidAmount(Money paidAmount) {
        return OrderAmount.builder()
                .totalAmount(totalAmount)
                .paidAmount(paidAmount)
                .couponAmount(couponAmount)
                .pointAmount(pointAmount)
                .shippingFee(shippingFee)
                .build();
    }

    private static Money resolveMoneyOrZero(Long value) {
        if (FormatValidator.hasNoValue(value)) {
            return Money.zero();
        }
        return Money.of(value);
    }
}
