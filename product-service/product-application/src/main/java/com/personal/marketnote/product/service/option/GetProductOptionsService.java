package com.personal.marketnote.product.service.option;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.product.domain.option.ProductOptionCategory;
import com.personal.marketnote.product.port.in.result.option.GetProductOptionsResult;
import com.personal.marketnote.product.port.in.usecase.option.GetProductOptionsUseCase;
import com.personal.marketnote.product.port.out.productoption.FindProductOptionCategoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true)
public class GetProductOptionsService implements GetProductOptionsUseCase {
    private final FindProductOptionCategoryPort findProductOptionCategoryPort;

    @Override
    public GetProductOptionsResult getProductOptions(Long productId) {
        List<ProductOptionCategory> categories
                = findProductOptionCategoryPort.findActiveWithOptionsByProductId(productId);

        return GetProductOptionsResult.from(categories);
    }
}
