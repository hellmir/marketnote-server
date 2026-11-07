package com.personal.marketnote.user.service.shippingaddress;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.kafka.event.ShippingAddressChangeAction;
import com.personal.marketnote.user.domain.shippingaddress.ShippingAddress;
import com.personal.marketnote.user.exception.ShippingAddressNotFoundException;
import com.personal.marketnote.user.port.in.usecase.shippingaddress.DeleteShippingAddressUseCase;
import com.personal.marketnote.user.port.out.event.PublishShippingAddressEventPort;
import com.personal.marketnote.user.port.out.shippingaddress.FindShippingAddressPort;
import com.personal.marketnote.user.port.out.shippingaddress.UpdateShippingAddressPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@Slf4j
@RequiredArgsConstructor
@UseCase
@Transactional(isolation = READ_COMMITTED)
public class DeleteShippingAddressService implements DeleteShippingAddressUseCase {
    private final FindShippingAddressPort findShippingAddressPort;
    private final UpdateShippingAddressPort updateShippingAddressPort;
    private final PublishShippingAddressEventPort publishShippingAddressEventPort;

    @Override
    public void deleteShippingAddress(Long shippingAddressId, Long userId) {
        ShippingAddress shippingAddress = findShippingAddressPort.findByIdAndUserId(shippingAddressId, userId)
                .orElseThrow(() -> {
                    log.warn("배송지를 찾을 수 없습니다. shippingAddressId={}, userId={}", shippingAddressId, userId);
                    return new ShippingAddressNotFoundException();
                });

        shippingAddress.delete();

        updateShippingAddressPort.update(shippingAddress);

        publishShippingAddressEventPort.publishShippingAddressChangedEvent(
                shippingAddressId, userId,
                shippingAddress.getRecipientName().getValue(), shippingAddress.getRecipientPhoneNumber().getValue(),
                shippingAddress.getAddress(), shippingAddress.getAddressDetail(),
                null,
                ShippingAddressChangeAction.DELETED
        );
    }
}
