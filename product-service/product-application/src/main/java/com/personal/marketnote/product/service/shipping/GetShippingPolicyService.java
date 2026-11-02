package com.personal.marketnote.product.service.shipping;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.product.domain.shipping.ShippingPolicy;
import com.personal.marketnote.product.exception.ShippingPolicyNotFoundException;
import com.personal.marketnote.product.port.in.result.shipping.GetShippingPolicyBySellerResult;
import com.personal.marketnote.product.port.in.result.shipping.GetShippingPolicyResult;
import com.personal.marketnote.product.port.in.usecase.shipping.GetShippingPolicyUseCase;
import com.personal.marketnote.product.port.out.shipping.FindShippingPolicyPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
public class GetShippingPolicyService implements GetShippingPolicyUseCase {

    private final FindShippingPolicyPort findShippingPolicyPort;

    @Override
    @Transactional(isolation = READ_COMMITTED, readOnly = true)
    public GetShippingPolicyResult getShippingPolicy(Long sellerId) {
        ShippingPolicy shippingPolicy = findShippingPolicy(sellerId);
        return GetShippingPolicyResult.from(shippingPolicy);
    }

    @Override
    @Transactional(isolation = READ_COMMITTED, readOnly = true)
    public List<GetShippingPolicyBySellerResult> getShippingPolicies(List<Long> sellerIds) {
        if (FormatValidator.hasNoValue(sellerIds)) {
            return List.of();
        }

        List<ShippingPolicy> policies = findShippingPolicyPort.findActiveBySellerIds(sellerIds);
        return policies.stream()
                .map(GetShippingPolicyBySellerResult::from)
                .toList();
    }

    private ShippingPolicy findShippingPolicy(Long sellerId) {
        return findShippingPolicyPort.findActiveBySellerId(sellerId)
                .orElseThrow(() -> new ShippingPolicyNotFoundException(sellerId));
    }
}
