package com.personal.marketnote.product.port.in.usecase.shipping;

import com.personal.marketnote.product.port.in.result.shipping.GetShippingPolicyResult;

public interface GetShippingPolicyUseCase {

    GetShippingPolicyResult getShippingPolicy(Long sellerId);
}
