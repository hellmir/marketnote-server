package com.personal.marketnote.product.service.pricepolicy;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.product.domain.pricepolicy.PricePolicy;
import com.personal.marketnote.product.exception.PricePolicyNotFoundException;
import com.personal.marketnote.product.port.in.usecase.pricepolicy.GetPricePolicyUseCase;
import com.personal.marketnote.product.port.out.pricepolicy.FindPricePolicyPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@Slf4j
@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true)
public class GetPricePolicyService implements GetPricePolicyUseCase {
    private final FindPricePolicyPort findPricePolicyPort;

    @Override
    public PricePolicy getPricePolicy(Long id) {
        return findPricePolicyPort.findById(id)
                .orElseThrow(() -> {
                    log.warn("가격 정책을 찾을 수 없습니다. pricePolicyId={}", id);
                    return new PricePolicyNotFoundException();
                });
    }

    @Override
    public PricePolicy getPricePolicy(List<Long> optionIds) {
        return findPricePolicyPort.findByOptionIds(optionIds)
                .orElseThrow(() -> {
                    log.warn("가격 정책을 찾을 수 없습니다. optionIds={}", optionIds);
                    return new PricePolicyNotFoundException();
                });
    }
}


