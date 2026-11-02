package com.personal.marketnote.product.port.in.usecase.shipping;

import com.personal.marketnote.product.port.in.result.shipping.GetShippingPolicyBySellerResult;
import com.personal.marketnote.product.port.in.result.shipping.GetShippingPolicyResult;

import java.util.List;

public interface GetShippingPolicyUseCase {

    GetShippingPolicyResult getShippingPolicy(Long sellerId);

    List<GetShippingPolicyBySellerResult> getShippingPolicies(List<Long> sellerIds);
}
