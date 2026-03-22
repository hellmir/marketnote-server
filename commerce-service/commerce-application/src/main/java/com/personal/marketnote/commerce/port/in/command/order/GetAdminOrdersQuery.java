package com.personal.marketnote.commerce.port.in.command.order;

import com.personal.marketnote.commerce.domain.order.OrderStatus;
import com.personal.marketnote.commerce.exception.InvalidOrderDateRangeException;
import com.personal.marketnote.common.utility.FormatValidator;

import java.time.LocalDateTime;

public record GetAdminOrdersQuery(
        Long sellerId,
        LocalDateTime startDate,
        LocalDateTime endDate,
        OrderStatus orderStatus
) {
    public static GetAdminOrdersQuery of(
            Long sellerId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            OrderStatus orderStatus
    ) {
        if (FormatValidator.hasValue(startDate)
                && FormatValidator.hasValue(endDate)
                && endDate.isBefore(startDate)) {
            throw new InvalidOrderDateRangeException();
        }
        return new GetAdminOrdersQuery(sellerId, startDate, endDate, orderStatus);
    }
}
