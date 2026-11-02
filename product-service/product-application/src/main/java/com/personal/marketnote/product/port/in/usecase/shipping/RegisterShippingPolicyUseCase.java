package com.personal.marketnote.product.port.in.usecase.shipping;

import com.personal.marketnote.product.port.in.command.RegisterShippingPolicyCommand;
import com.personal.marketnote.product.port.in.result.shipping.RegisterShippingPolicyResult;

public interface RegisterShippingPolicyUseCase {

    RegisterShippingPolicyResult registerShippingPolicy(Long sellerId, RegisterShippingPolicyCommand command);
}
