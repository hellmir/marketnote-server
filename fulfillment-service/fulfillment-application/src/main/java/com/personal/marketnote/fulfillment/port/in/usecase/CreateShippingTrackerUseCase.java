package com.personal.marketnote.fulfillment.port.in.usecase;

import com.personal.marketnote.fulfillment.port.in.command.CreateShippingTrackerCommand;

public interface CreateShippingTrackerUseCase {
    void createShippingTracker(CreateShippingTrackerCommand command);
}
