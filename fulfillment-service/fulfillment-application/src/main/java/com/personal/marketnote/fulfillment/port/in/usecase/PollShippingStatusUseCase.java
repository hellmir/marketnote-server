package com.personal.marketnote.fulfillment.port.in.usecase;

import com.personal.marketnote.fulfillment.port.in.command.PollShippingStatusCommand;

public interface PollShippingStatusUseCase {
    void pollShippingStatuses(PollShippingStatusCommand command);
}
