package com.personal.marketnote.user.service.shippingaddress;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.user.domain.shippingaddress.ShippingAddress;
import com.personal.marketnote.user.exception.ShippingAddressNotFoundException;
import com.personal.marketnote.user.port.in.result.shippingaddress.GetShippingAddressResult;
import com.personal.marketnote.user.port.in.usecase.shippingaddress.GetShippingAddressUseCase;
import com.personal.marketnote.user.port.out.shippingaddress.FindShippingAddressPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@Slf4j
@RequiredArgsConstructor
@UseCase
@Transactional(isolation = READ_COMMITTED, readOnly = true)
public class GetShippingAddressService implements GetShippingAddressUseCase {
    private final FindShippingAddressPort findShippingAddressPort;

    @Override
    public GetShippingAddressResult getShippingAddress(Long shippingAddressId, Long userId) {
        ShippingAddress shippingAddress = findShippingAddressPort.findByIdAndUserId(shippingAddressId, userId)
                .orElseThrow(() -> {
                    log.warn("배송지를 찾을 수 없습니다. shippingAddressId={}, userId={}", shippingAddressId, userId);
                    return new ShippingAddressNotFoundException();
                });

        return GetShippingAddressResult.from(shippingAddress);
    }
}
