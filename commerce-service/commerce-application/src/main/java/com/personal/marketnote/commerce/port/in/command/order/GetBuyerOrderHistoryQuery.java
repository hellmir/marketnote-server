package com.personal.marketnote.commerce.port.in.command.order;

import com.personal.marketnote.commerce.domain.order.OrderPeriod;
import com.personal.marketnote.commerce.domain.order.OrderStatus;
import com.personal.marketnote.commerce.domain.order.OrderStatusFilter;
import com.personal.marketnote.common.utility.FormatValidator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public record GetBuyerOrderHistoryQuery(
        Long buyerId,
        OrderPeriod period,
        OrderStatusFilter status,
        String productName
) {
    public static GetBuyerOrderHistoryQuery of(
            Long buyerId,
            OrderPeriod period,
            OrderStatusFilter status,
            String productName
    ) {
        return new GetBuyerOrderHistoryQuery(buyerId, period, status, productName);
    }

    public LocalDateTime calculateStartDate(LocalDate now) {
        OrderPeriod resolved = FormatValidator.hasValue(period)
                ? period
                : OrderPeriod.ALL;

        return resolved.startDate(now);
    }

    public LocalDateTime calculateEndDate(LocalDate now) {
        return LocalDateTime.of(now.plusDays(1), LocalTime.MIDNIGHT);
    }

    public List<OrderStatus> resolveStatuses() {
        OrderStatusFilter resolved = FormatValidator.hasValue(status)
                ? status
                : OrderStatusFilter.ALL;

        return resolved.toStatuses();
    }

    public String resolvedProductName() {
        return FormatValidator.hasValue(productName)
                ? productName.trim()
                : null;
    }
}
