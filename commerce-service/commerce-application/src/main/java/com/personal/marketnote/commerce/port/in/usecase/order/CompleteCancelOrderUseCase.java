package com.personal.marketnote.commerce.port.in.usecase.order;

import com.personal.marketnote.commerce.port.in.command.order.CompleteCancelOrderCommand;

public interface CompleteCancelOrderUseCase {
    void completeCancellation(CompleteCancelOrderCommand command);
}
