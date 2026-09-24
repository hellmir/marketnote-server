package com.personal.marketnote.product.service.product;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.product.domain.product.Product;
import com.personal.marketnote.product.exception.NotProductOwnerException;
import com.personal.marketnote.product.port.in.result.product.GetProductKeyResult;
import com.personal.marketnote.product.port.in.usecase.product.GetProductKeyUseCase;
import com.personal.marketnote.product.port.in.usecase.product.GetProductUseCase;
import com.personal.marketnote.product.port.out.product.FindProductPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static com.personal.marketnote.common.domain.exception.ExceptionCode.FIRST_ERROR_CODE;
import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true)
public class GetProductKeyService implements GetProductKeyUseCase {
    private final GetProductUseCase getProductUseCase;
    private final FindProductPort findProductPort;

    @Override
    public GetProductKeyResult getProductKey(Long id, Long userId, boolean isAdmin) {
        if (!isAdmin && !findProductPort.existsByIdAndSellerId(id, userId)) {
            throw new NotProductOwnerException(FIRST_ERROR_CODE, id);
        }

        Product product = getProductUseCase.getProduct(id);
        return GetProductKeyResult.from(product);
    }
}
