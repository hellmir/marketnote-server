package com.personal.marketnote.fulfillment.service;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.fulfillment.domain.delivery.FulfillmentWorkStatus;
import com.personal.marketnote.fulfillment.port.in.command.GetFulfillmentWorkStatusCommand;
import com.personal.marketnote.fulfillment.port.in.result.GetFulfillmentWorkStatusResult;
import com.personal.marketnote.fulfillment.port.in.usecase.GetFulfillmentWorkStatusUseCase;
import com.personal.marketnote.fulfillment.port.out.delivery.FindFulfillmentDeliveryRegistrationPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true)
public class GetFulfillmentWorkStatusService implements GetFulfillmentWorkStatusUseCase {
    private final FindFulfillmentDeliveryRegistrationPort findFulfillmentDeliveryRegistrationPort;

    @Override
    public GetFulfillmentWorkStatusResult getWorkStatus(GetFulfillmentWorkStatusCommand command) {
        return findFulfillmentDeliveryRegistrationPort.findByOrderId(command.orderId())
                .map(registration -> new GetFulfillmentWorkStatusResult(
                        command.orderId(),
                        registration.getWorkStatus().name()
                ))
                .orElseGet(() -> new GetFulfillmentWorkStatusResult(
                        command.orderId(),
                        FulfillmentWorkStatus.NOT_REGISTERED.name()
                ));
    }
}
