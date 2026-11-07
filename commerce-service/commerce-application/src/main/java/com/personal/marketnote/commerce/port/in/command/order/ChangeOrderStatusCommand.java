package com.personal.marketnote.commerce.port.in.command.order;

import com.personal.marketnote.commerce.domain.order.OrderStatus;
import com.personal.marketnote.commerce.domain.order.OrderStatusReasonCategory;
import com.personal.marketnote.commerce.domain.payment.Payment;
import com.personal.marketnote.common.utility.FormatValidator;
import lombok.Builder;

import java.util.List;

@Builder
public record ChangeOrderStatusCommand(
        Long id,
        List<Long> pricePolicyIds,
        OrderStatus orderStatus,
        OrderStatusReasonCategory reasonCategory,
        String reason,
        Boolean skipSubsequentProcesses,
        Long paidAmount
) {
    public boolean isPartialProductChange() {
        return FormatValidator.hasValue(pricePolicyIds);
    }

    public boolean shouldSkipSubsequentProcesses() {
        return Boolean.TRUE.equals(skipSubsequentProcesses);
    }

    public static ChangeOrderStatusCommand toExecuting(Long orderId) {
        return ChangeOrderStatusCommand.builder()
                .id(orderId)
                .orderStatus(OrderStatus.PAYMENT_EXECUTING)
                .skipSubsequentProcesses(true)
                .build();
    }

    public static ChangeOrderStatusCommand toPaid(Payment payment) {
        return ChangeOrderStatusCommand.builder()
                .id(payment.getOrderId())
                .orderStatus(OrderStatus.PAID)
                .paidAmount(payment.getPaymentAmount().getValue())
                .build();
    }

    public static ChangeOrderStatusCommand toPaidWithoutAmount(Long orderId) {
        return ChangeOrderStatusCommand.builder()
                .id(orderId)
                .orderStatus(OrderStatus.PAID)
                .build();
    }

    public static ChangeOrderStatusCommand toPaidSkipSubsequent(Payment payment) {
        return ChangeOrderStatusCommand.builder()
                .id(payment.getOrderId())
                .orderStatus(OrderStatus.PAID)
                .skipSubsequentProcesses(true)
                .paidAmount(payment.getPaymentAmount().getValue())
                .build();
    }

    public static ChangeOrderStatusCommand toFailed(Long orderId) {
        return ChangeOrderStatusCommand.builder()
                .id(orderId)
                .orderStatus(OrderStatus.FAILED)
                .build();
    }

    public static ChangeOrderStatusCommand toConfirmed(Long orderId) {
        return ChangeOrderStatusCommand.builder()
                .id(orderId)
                .orderStatus(OrderStatus.CONFIRMED)
                .build();
    }
}
