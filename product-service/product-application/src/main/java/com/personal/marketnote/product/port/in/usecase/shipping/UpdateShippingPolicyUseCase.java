package com.personal.marketnote.product.port.in.usecase.shipping;

import com.personal.marketnote.product.port.in.command.UpdateShippingPolicyCommand;
import com.personal.marketnote.product.port.in.result.shipping.UpdateShippingPolicyResult;

public interface UpdateShippingPolicyUseCase {

    UpdateShippingPolicyResult updateShippingPolicy(Long sellerId, UpdateShippingPolicyCommand command);
}
