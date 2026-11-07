package com.personal.marketnote.user.service.shippingaddress;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.user.domain.shippingaddress.ShippingAddress;
import com.personal.marketnote.user.exception.ShippingAddressNotFoundException;
import com.personal.marketnote.user.port.in.usecase.shippingaddress.SetDefaultShippingAddressUseCase;
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
public class SetDefaultShippingAddressService implements SetDefaultShippingAddressUseCase {
    private final FindShippingAddressPort findShippingAddressPort;
    private final UpdateShippingAddressPort updateShippingAddressPort;

    @Override
    public void setDefaultShippingAddress(Long shippingAddressId, Long userId) {
        ShippingAddress targetAddress = findShippingAddressPort.findByIdAndUserId(shippingAddressId, userId)
                .orElseThrow(() -> {
                    log.warn("배송지를 찾을 수 없습니다. shippingAddressId={}, userId={}", shippingAddressId, userId);
                    return new ShippingAddressNotFoundException();
                });

        if (targetAddress.isDefault()) {
            return;
        }

        findShippingAddressPort.findDefaultsByUserId(userId)
                .forEach(currentDefault -> {
                    currentDefault.unsetAsDefault();
                    updateShippingAddressPort.update(currentDefault);
                });

        targetAddress.setAsDefault();
        updateShippingAddressPort.update(targetAddress);
    }
}
