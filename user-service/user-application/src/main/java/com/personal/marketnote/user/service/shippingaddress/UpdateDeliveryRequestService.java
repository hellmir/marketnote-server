package com.personal.marketnote.user.service.shippingaddress;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.user.domain.shippingaddress.ShippingAddress;
import com.personal.marketnote.user.exception.ShippingAddressNotFoundException;
import com.personal.marketnote.user.port.in.command.shippingaddress.UpdateDeliveryRequestCommand;
import com.personal.marketnote.user.port.in.usecase.shippingaddress.UpdateDeliveryRequestUseCase;
import com.personal.marketnote.user.port.out.shippingaddress.FindShippingAddressPort;
import com.personal.marketnote.user.port.out.shippingaddress.UpdateShippingAddressPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@RequiredArgsConstructor
@UseCase
@Transactional(isolation = READ_COMMITTED)
public class UpdateDeliveryRequestService implements UpdateDeliveryRequestUseCase {
    private final FindShippingAddressPort findShippingAddressPort;
    private final UpdateShippingAddressPort updateShippingAddressPort;

    @Override
    public void updateDeliveryRequest(Long shippingAddressId, Long userId, UpdateDeliveryRequestCommand command) {
        ShippingAddress shippingAddress = findShippingAddressPort.findByIdAndUserId(shippingAddressId, userId)
                .orElseThrow(() -> new ShippingAddressNotFoundException(shippingAddressId));

        shippingAddress.updateDeliveryRequest(
                command.deliveryRequestType(),
                command.deliveryRequestMessage()
        );

        updateShippingAddressPort.update(shippingAddress);
    }
}
