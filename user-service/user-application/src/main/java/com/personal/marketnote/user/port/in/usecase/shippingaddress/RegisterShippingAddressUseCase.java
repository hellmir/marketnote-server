package com.personal.marketnote.user.port.in.usecase.shippingaddress;

import com.personal.marketnote.user.port.in.command.shippingaddress.RegisterShippingAddressCommand;
import com.personal.marketnote.user.port.in.result.shippingaddress.RegisterShippingAddressResult;

public interface RegisterShippingAddressUseCase {
    RegisterShippingAddressResult registerShippingAddress(RegisterShippingAddressCommand command);
}
