package com.personal.marketnote.common.kafka.event;

public record FulfillmentDeliveryWorkStatusChangedEvent(
        Long orderId,
        String workStatus
) {
}
