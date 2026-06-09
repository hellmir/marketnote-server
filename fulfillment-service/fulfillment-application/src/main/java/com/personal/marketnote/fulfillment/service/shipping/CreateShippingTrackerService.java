package com.personal.marketnote.fulfillment.service.shipping;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.fulfillment.domain.shipping.ShippingTracker;
import com.personal.marketnote.fulfillment.domain.shipping.ShippingTrackerCreateState;
import com.personal.marketnote.fulfillment.port.in.command.CreateShippingTrackerCommand;
import com.personal.marketnote.fulfillment.port.in.usecase.CreateShippingTrackerUseCase;
import com.personal.marketnote.fulfillment.port.out.shipping.SaveShippingTrackerPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED)
public class CreateShippingTrackerService implements CreateShippingTrackerUseCase {
    private final SaveShippingTrackerPort saveShippingTrackerPort;

    @Override
    public void createShippingTracker(CreateShippingTrackerCommand command) {
        ShippingTracker shippingTracker = ShippingTracker.from(
                ShippingTrackerCreateState.builder()
                        .orderId(command.orderId())
                        .build()
        );
        saveShippingTrackerPort.save(shippingTracker);
    }
}
