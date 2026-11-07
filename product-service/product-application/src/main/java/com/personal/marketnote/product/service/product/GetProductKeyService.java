package com.personal.marketnote.product.service.product;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.product.domain.product.Product;
import com.personal.marketnote.product.exception.NotProductOwnerException;
import com.personal.marketnote.product.port.in.result.product.GetProductKeyResult;
import com.personal.marketnote.product.port.in.usecase.product.GetProductKeyUseCase;
import com.personal.marketnote.product.port.in.usecase.product.GetProductUseCase;
import com.personal.marketnote.product.port.out.product.FindProductPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@Slf4j
@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true)
public class GetProductKeyService implements GetProductKeyUseCase {
    private final GetProductUseCase getProductUseCase;
    private final FindProductPort findProductPort;

    @Override
    public GetProductKeyResult getProductKey(Long id, Long userId, boolean isAdmin) {
        if (!isAdmin && !findProductPort.existsByIdAndSellerId(id, userId)) {
            log.warn("상품 소유자가 아닙니다. productId={}", id);
            throw new NotProductOwnerException();
        }

        Product product = getProductUseCase.getProduct(id);
        return GetProductKeyResult.from(product);
    }
}
