package com.personal.marketnote.product.service.pricepolicy;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.product.domain.pricepolicy.PricePolicy;
import com.personal.marketnote.product.exception.PricePolicyNotFoundException;
import com.personal.marketnote.product.port.in.usecase.pricepolicy.GetPricePolicyUseCase;
import com.personal.marketnote.product.port.out.pricepolicy.FindPricePolicyPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true)
public class GetPricePolicyService implements GetPricePolicyUseCase {
    private final FindPricePolicyPort findPricePolicyPort;

    @Override
    public PricePolicy getPricePolicy(Long id) {
        return findPricePolicyPort.findById(id)
                .orElseThrow(() -> new PricePolicyNotFoundException(id));
    }

    @Override
    public PricePolicy getPricePolicy(List<Long> optionIds) {
        return findPricePolicyPort.findByOptionIds(optionIds)
                .orElseThrow(() -> new PricePolicyNotFoundException(-1L));
    }
}


