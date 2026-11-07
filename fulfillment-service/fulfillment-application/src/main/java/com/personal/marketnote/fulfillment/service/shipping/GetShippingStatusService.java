package com.personal.marketnote.fulfillment.service.shipping;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.fulfillment.domain.exception.ShippingTrackerNotFoundException;
import com.personal.marketnote.fulfillment.domain.shipping.ShippingTracker;
import com.personal.marketnote.fulfillment.port.in.command.GetShippingStatusCommand;
import com.personal.marketnote.fulfillment.port.in.result.GetShippingStatusResult;
import com.personal.marketnote.fulfillment.port.in.usecase.GetShippingStatusUseCase;
import com.personal.marketnote.fulfillment.port.out.shipping.FindShippingTrackerPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@Slf4j
@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true)
public class GetShippingStatusService implements GetShippingStatusUseCase {

    private final FindShippingTrackerPort findShippingTrackerPort;

    @Override
    public GetShippingStatusResult getShippingStatus(GetShippingStatusCommand command) {
        ShippingTracker tracker = findShippingTrackerPort.findByOrderId(command.orderId())
                .orElseThrow(() -> {
                    log.warn("배송 추적 정보를 찾을 수 없습니다. orderId={}", command.orderId());
                    return new ShippingTrackerNotFoundException();
                });

        return new GetShippingStatusResult(
                tracker.getOrderId(),
                tracker.getShippingStatus().name(),
                tracker.isPreparing(),
                tracker.getTrackingNumberValue(),
                tracker.getCarrierCodeValue(),
                tracker.getLastPolledAt()
        );
    }
}
