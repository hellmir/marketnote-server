package com.personal.marketnote.product.service.pricepolicy;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.product.exception.NotProductOwnerException;
import com.personal.marketnote.product.port.in.usecase.pricepolicy.DeletePricePolicyUseCase;
import com.personal.marketnote.product.port.out.event.PublishProductEventPort;
import com.personal.marketnote.product.port.out.pricepolicy.DeletePricePolicyPort;
import com.personal.marketnote.product.port.out.product.FindProductPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static com.personal.marketnote.common.domain.exception.ExceptionCode.FIRST_ERROR_CODE;
import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED)
public class DeletePricePolicyService implements DeletePricePolicyUseCase {
    private final FindProductPort findProductPort;
    private final DeletePricePolicyPort deletePricePolicyPort;
    private final PublishProductEventPort publishProductEventPort;

    @Override
    public void delete(Long userId, boolean isAdmin, Long productId, Long pricePolicyId) {
        if (!isAdmin && !findProductPort.existsByIdAndSellerId(productId, userId)) {
            throw new NotProductOwnerException(FIRST_ERROR_CODE, productId);
        }

        deletePricePolicyPort.deleteById(pricePolicyId);
        publishProductEventPort.publishPricePolicyDeletedEvent(productId, pricePolicyId);
    }
}
