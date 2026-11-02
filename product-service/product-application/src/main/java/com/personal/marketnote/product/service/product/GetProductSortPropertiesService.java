package com.personal.marketnote.product.service.product;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.product.domain.product.ProductSortProperty;
import com.personal.marketnote.product.port.in.result.product.GetProductSortPropertiesResult;
import com.personal.marketnote.product.port.in.usecase.product.GetProductSortPropertiesUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true)
public class GetProductSortPropertiesService implements GetProductSortPropertiesUseCase {
    @Override
    public GetProductSortPropertiesResult getProductSortProperties() {
        return GetProductSortPropertiesResult.from(ProductSortProperty.values());
    }
}
