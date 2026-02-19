package com.personal.marketnote.user.port.in.usecase.shippingaddress;

public interface DeleteShippingAddressUseCase {
    void deleteShippingAddress(Long shippingAddressId, Long userId);
}
