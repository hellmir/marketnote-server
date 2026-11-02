package com.personal.marketnote.product.service.product;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.product.domain.product.ProductSearchTarget;
import com.personal.marketnote.product.port.in.result.product.GetProductSearchTargetsResult;
import com.personal.marketnote.product.port.in.usecase.product.GetProductSearchTargetsUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true)
public class GetProductSearchTargetsService implements GetProductSearchTargetsUseCase {
    @Override
    public GetProductSearchTargetsResult getProductSearchTargets() {
        return GetProductSearchTargetsResult.from(ProductSearchTarget.values());
    }
}
