package com.personal.marketnote.user.port.in.usecase.shippingaddress;

import com.personal.marketnote.user.port.in.command.shippingaddress.UpdateShippingAddressCommand;

public interface UpdateShippingAddressUseCase {
    void updateShippingAddress(Long shippingAddressId, Long userId, UpdateShippingAddressCommand command);
}
