package com.personal.marketnote.product.service.option;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.product.exception.NotProductOwnerException;
import com.personal.marketnote.product.port.in.usecase.option.DeleteProductOptionsUseCase;
import com.personal.marketnote.product.port.out.product.FindProductPort;
import com.personal.marketnote.product.port.out.productoption.DeleteProductOptionCategoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static com.personal.marketnote.common.domain.exception.ExceptionCode.FIRST_ERROR_CODE;
import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

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
            throw new NotProductOwnerException(FIRST_ERROR_CODE, productId);
        }

        deleteProductOptionCategoryPort.deleteById(optionCategoryId);
    }
}
