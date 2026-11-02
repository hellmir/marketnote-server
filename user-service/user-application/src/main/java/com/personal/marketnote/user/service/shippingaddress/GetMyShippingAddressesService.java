package com.personal.marketnote.user.service.shippingaddress;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.user.domain.shippingaddress.ShippingAddress;
import com.personal.marketnote.user.port.in.result.shippingaddress.GetMyShippingAddressesResult;
import com.personal.marketnote.user.port.in.usecase.shippingaddress.GetMyShippingAddressesUseCase;
import com.personal.marketnote.user.port.out.shippingaddress.FindShippingAddressPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@RequiredArgsConstructor
@UseCase
@Transactional(isolation = READ_COMMITTED, readOnly = true)
public class GetMyShippingAddressesService implements GetMyShippingAddressesUseCase {
    private final FindShippingAddressPort findShippingAddressPort;

    @Override
    public GetMyShippingAddressesResult getMyShippingAddresses(Long userId) {
        List<ShippingAddress> shippingAddresses = new ArrayList<>(findShippingAddressPort.findAllByUserId(userId));

        shippingAddresses.sort(
                Comparator.comparing(ShippingAddress::isDefault, Comparator.reverseOrder())
                        .thenComparing(sa -> sa.getAddressType().ordinal())
        );

        return GetMyShippingAddressesResult.from(shippingAddresses);
    }
}
