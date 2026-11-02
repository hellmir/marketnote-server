package com.personal.marketnote.fulfillment.port.in.command;

public record GetShippingStatusCommand(
        Long orderId
) {
}
