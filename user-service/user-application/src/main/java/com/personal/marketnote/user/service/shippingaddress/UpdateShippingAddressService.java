package com.personal.marketnote.user.service.shippingaddress;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.kafka.event.ShippingAddressChangeAction;
import com.personal.marketnote.user.domain.shippingaddress.ShippingAddress;
import com.personal.marketnote.user.domain.shippingaddress.ShippingAddressRegionType;
import com.personal.marketnote.user.exception.ShippingAddressNotFoundException;
import com.personal.marketnote.user.port.in.command.shippingaddress.UpdateShippingAddressCommand;
import com.personal.marketnote.user.port.in.usecase.shippingaddress.UpdateShippingAddressUseCase;
import com.personal.marketnote.user.port.out.event.PublishShippingAddressEventPort;
import com.personal.marketnote.user.port.out.shippingaddress.ClassifyShippingAddressRegionPort;
import com.personal.marketnote.user.port.out.shippingaddress.FindShippingAddressPort;
import com.personal.marketnote.user.port.out.shippingaddress.UpdateShippingAddressPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@RequiredArgsConstructor
@UseCase
@Transactional(isolation = READ_COMMITTED)
public class UpdateShippingAddressService implements UpdateShippingAddressUseCase {
    private final FindShippingAddressPort findShippingAddressPort;
    private final UpdateShippingAddressPort updateShippingAddressPort;
    private final PublishShippingAddressEventPort publishShippingAddressEventPort;
    private final ClassifyShippingAddressRegionPort classifyShippingAddressRegionPort;

    @Override
    public void updateShippingAddress(Long shippingAddressId, Long userId, UpdateShippingAddressCommand command) {
        ShippingAddress shippingAddress = findShippingAddressPort.findByIdAndUserId(shippingAddressId, userId)
                .orElseThrow(() -> new ShippingAddressNotFoundException(shippingAddressId));

        shippingAddress.update(
                command.address(),
                command.addressDetail(),
                command.companyName(),
                command.addressAlias(),
                command.recipientName(),
                command.recipientPhoneNumber(),
                command.deliveryRequestType(),
                command.deliveryRequestMessage()
        );

        ShippingAddressRegionType regionType = classifyShippingAddressRegionPort.classify(command.address());
        shippingAddress.assignRegionType(regionType);

        updateShippingAddressPort.update(shippingAddress);

        publishShippingAddressEventPort.publishShippingAddressChangedEvent(
                shippingAddressId, userId,
                shippingAddress.getRecipientName(), shippingAddress.getRecipientPhoneNumber(),
                shippingAddress.getAddress(), shippingAddress.getAddressDetail(),
                regionType.name(),
                ShippingAddressChangeAction.UPDATED
        );
    }
}
