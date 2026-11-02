package com.personal.marketnote.product.service.pricepolicy;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.product.domain.pricepolicy.PricePolicy;
import com.personal.marketnote.product.port.in.result.pricepolicy.GetPricePoliciesResult;
import com.personal.marketnote.product.port.in.result.pricepolicy.GetProductPricePolicyWithOptionsResult;
import com.personal.marketnote.product.port.in.usecase.pricepolicy.GetPricePoliciesUseCase;
import com.personal.marketnote.product.port.out.pricepolicy.FindPricePoliciesPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true)
public class GetPricePoliciesService implements GetPricePoliciesUseCase {
    private final FindPricePoliciesPort findPricePoliciesPort;

    @Override
    public GetPricePoliciesResult getPricePoliciesAndOptions(Long productId) {
        List<GetProductPricePolicyWithOptionsResult> items = findPricePoliciesPort.findByProductId(productId)
                .stream()
                .map(
                        pricePolicy -> new GetProductPricePolicyWithOptionsResult(
                                pricePolicy.getId(),
                                pricePolicy.getPrice().getValue(),
                                pricePolicy.getDiscountPrice().getValue(),
                                pricePolicy.getAccumulatedPoint().getValue(),
                                pricePolicy.getDiscountRate().getValue(),
                                pricePolicy.getOptionIds()
                        )
                )
                .toList();

        return GetPricePoliciesResult.of(items);
    }

    @Override
    public List<PricePolicy> getPricePoliciesAndOptions(List<Long> ids) {
        return findPricePoliciesPort.findByIds(ids);
    }
}


