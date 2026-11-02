package com.personal.marketnote.fulfillment.service.vendor;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.fulfillment.port.in.command.vendor.CancelFulfillmentDeliveryCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.CancelFulfillmentDeliveryResult;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.CancelFulfillmentDeliveryUseCase;
import com.personal.marketnote.fulfillment.port.out.vendor.CancelFulfillmentDeliveryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED)
public class CancelFulfillmentDeliveryService implements CancelFulfillmentDeliveryUseCase {
    private final CancelFulfillmentDeliveryPort cancelFulfillmentDeliveryPort;

    @Override
    public CancelFulfillmentDeliveryResult cancelDelivery(CancelFulfillmentDeliveryCommand command) {
        return cancelFulfillmentDeliveryPort.cancelDelivery(command);
    }
}
