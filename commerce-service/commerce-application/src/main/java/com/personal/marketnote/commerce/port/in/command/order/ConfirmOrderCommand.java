package com.personal.marketnote.commerce.port.in.command.order;

import lombok.Builder;

@Builder
public record ConfirmOrderCommand(
        Long id,
        Long buyerId
) {
}
