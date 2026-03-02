package com.personal.marketnote.commerce.port.in.command.order;

import com.personal.marketnote.commerce.domain.order.CourierCompany;
import lombok.Builder;

@Builder
public record RegisterTrackingInfoCommand(
        Long orderId,
        CourierCompany courierCompany,
        String trackingNumber
) {
}
