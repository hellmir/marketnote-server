package com.personal.marketnote.fulfillment.service;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.fulfillment.domain.FulfillmentAccessToken;
import com.personal.marketnote.fulfillment.domain.delivery.FulfillmentDeliveryRegistration;
import com.personal.marketnote.fulfillment.domain.shipping.ShippingTracker;
import com.personal.marketnote.fulfillment.exception.FulfillmentDeliveryCancellationNotAllowedException;
import com.personal.marketnote.fulfillment.exception.FulfillmentDeliveryRegistrationNotFoundException;
import com.personal.marketnote.fulfillment.port.in.command.CancelInternalFulfillmentDeliveryCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.CancelFulfillmentDeliveryCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.CancelFulfillmentDeliveryItemCommand;
import com.personal.marketnote.fulfillment.port.in.result.CancelInternalFulfillmentDeliveryResult;
import com.personal.marketnote.fulfillment.port.in.usecase.CancelInternalFulfillmentDeliveryUseCase;
import com.personal.marketnote.fulfillment.port.out.delivery.FindFulfillmentDeliveryRegistrationPort;
import com.personal.marketnote.fulfillment.port.out.shipping.FindShippingTrackerPort;
import com.personal.marketnote.fulfillment.port.out.shipping.UpdateShippingTrackerPort;
import com.personal.marketnote.fulfillment.port.out.vendor.CancelFulfillmentDeliveryPort;
import com.personal.marketnote.fulfillment.port.out.vendor.DisconnectFulfillmentAuthPort;
import com.personal.marketnote.fulfillment.port.out.vendor.GetFulfillmentCustomerCodePort;
import com.personal.marketnote.fulfillment.port.out.vendor.RequestFulfillmentAuthPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED)
public class CancelInternalFulfillmentDeliveryService implements CancelInternalFulfillmentDeliveryUseCase {
    private final FindFulfillmentDeliveryRegistrationPort findFulfillmentDeliveryRegistrationPort;
    private final GetFulfillmentCustomerCodePort getFulfillmentCustomerCodePort;
    private final RequestFulfillmentAuthPort requestFulfillmentAuthPort;
    private final DisconnectFulfillmentAuthPort disconnectFulfillmentAuthPort;
    private final CancelFulfillmentDeliveryPort cancelFulfillmentDeliveryPort;
    private final FindShippingTrackerPort findShippingTrackerPort;
    private final UpdateShippingTrackerPort updateShippingTrackerPort;

    @Override
    public CancelInternalFulfillmentDeliveryResult cancelDelivery(CancelInternalFulfillmentDeliveryCommand command) {
        FulfillmentDeliveryRegistration registration = findRegistration(command.orderId());
        validateCancellable(registration);

        FulfillmentAccessToken accessToken = requestFulfillmentAuthPort.requestAccessToken();
        try {
            String customerCode = getFulfillmentCustomerCodePort.getCustomerCode();
            CancelFulfillmentDeliveryCommand cancelCommand = buildCancelCommand(
                    customerCode, accessToken.getValue(), command.orderId()
            );
            cancelFulfillmentDeliveryPort.cancelDelivery(cancelCommand);

            cancelShippingTrackerIfActive(command.orderId());

            return new CancelInternalFulfillmentDeliveryResult(command.orderId(), true, "출고 취소 성공");
        } finally {
            disconnectFulfillmentAuthPort.disconnectAccessToken(accessToken.getValue());
        }
    }

    private void cancelShippingTrackerIfActive(Long orderId) {
        findShippingTrackerPort.findByOrderId(orderId).ifPresent(tracker -> {
            if (!tracker.isPreparing() && !tracker.isShipping()) {
                return;
            }
            tracker.cancel();
            updateShippingTrackerPort.update(tracker);
        });
    }

    private FulfillmentDeliveryRegistration findRegistration(Long orderId) {
        return findFulfillmentDeliveryRegistrationPort.findByOrderId(orderId)
                .orElseThrow(() -> new FulfillmentDeliveryRegistrationNotFoundException(orderId));
    }

    private void validateCancellable(FulfillmentDeliveryRegistration registration) {
        if (!registration.isCancellable()) {
            throw new FulfillmentDeliveryCancellationNotAllowedException(registration.getWorkStatus());
        }
    }

    private CancelFulfillmentDeliveryCommand buildCancelCommand(String customerCode, String accessToken, Long orderId) {
        String orderNumber = String.valueOf(orderId);
        CancelFulfillmentDeliveryItemCommand itemCommand = CancelFulfillmentDeliveryItemCommand.of(orderNumber, orderNumber);
        return CancelFulfillmentDeliveryCommand.of(customerCode, accessToken, List.of(itemCommand));
    }
}
