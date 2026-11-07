package com.personal.marketnote.product.service.option;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.product.exception.NotProductOwnerException;
import com.personal.marketnote.product.port.in.usecase.option.DeleteProductOptionsUseCase;
import com.personal.marketnote.product.port.out.product.FindProductPort;
import com.personal.marketnote.product.port.out.productoption.DeleteProductOptionCategoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@Slf4j
@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED)
public class DeleteProductOptionsService implements DeleteProductOptionsUseCase {
    private final FindProductPort findProductPort;
    private final DeleteProductOptionCategoryPort deleteProductOptionCategoryPort;

    @Override
    public void deleteProductOptions(
            Long userId, boolean isAdmin, Long productId, Long optionCategoryId
    ) {
        if (!isAdmin && !findProductPort.existsByIdAndSellerId(productId, userId)) {
            log.warn("상품 소유자가 아닙니다. productId={}", productId);
            throw new NotProductOwnerException();
        }

        deleteProductOptionCategoryPort.deleteById(optionCategoryId);
    }
}
