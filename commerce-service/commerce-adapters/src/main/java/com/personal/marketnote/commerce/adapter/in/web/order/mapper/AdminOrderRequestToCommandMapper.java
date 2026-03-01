package com.personal.marketnote.commerce.adapter.in.web.order.mapper;

import com.personal.marketnote.commerce.domain.order.OrderStatus;
import com.personal.marketnote.commerce.port.in.command.order.GetAdminOrdersQuery;

import java.time.LocalDateTime;

public class AdminOrderRequestToCommandMapper {
    private AdminOrderRequestToCommandMapper() {
    }

    public static GetAdminOrdersQuery mapToQuery(
            Long sellerId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            OrderStatus orderStatus
    ) {
        return GetAdminOrdersQuery.of(sellerId, startDate, endDate, orderStatus);
    }
}
