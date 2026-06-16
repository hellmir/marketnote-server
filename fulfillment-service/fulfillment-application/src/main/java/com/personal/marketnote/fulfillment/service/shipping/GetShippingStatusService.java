package com.personal.marketnote.fulfillment.service.shipping;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.fulfillment.domain.exception.ShippingTrackerNotFoundException;
import com.personal.marketnote.fulfillment.domain.shipping.ShippingTracker;
import com.personal.marketnote.fulfillment.port.in.command.GetShippingStatusCommand;
import com.personal.marketnote.fulfillment.port.in.result.GetShippingStatusResult;
import com.personal.marketnote.fulfillment.port.in.usecase.GetShippingStatusUseCase;
import com.personal.marketnote.fulfillment.port.out.shipping.FindShippingTrackerPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true)
public class GetShippingStatusService implements GetShippingStatusUseCase {

    private final FindShippingTrackerPort findShippingTrackerPort;

    @Override
    public GetShippingStatusResult getShippingStatus(GetShippingStatusCommand command) {
        ShippingTracker tracker = findShippingTrackerPort.findByOrderId(command.orderId())
                .orElseThrow(() -> new ShippingTrackerNotFoundException(command.orderId()));

        return new GetShippingStatusResult(
                tracker.getOrderId(),
                tracker.getShippingStatus().name(),
                tracker.isPreparing(),
                tracker.getTrackingNumber(),
                tracker.getCarrierCode(),
                tracker.getLastPolledAt()
        );
    }
}
