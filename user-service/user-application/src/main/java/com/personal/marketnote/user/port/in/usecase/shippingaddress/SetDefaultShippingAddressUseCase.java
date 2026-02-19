package com.personal.marketnote.user.port.in.usecase.shippingaddress;

public interface SetDefaultShippingAddressUseCase {
    void setDefaultShippingAddress(Long shippingAddressId, Long userId);
}
