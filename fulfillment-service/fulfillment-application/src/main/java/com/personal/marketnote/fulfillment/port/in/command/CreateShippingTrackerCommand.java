package com.personal.marketnote.fulfillment.port.in.command;

public record CreateShippingTrackerCommand(
        Long orderId
) {
}
