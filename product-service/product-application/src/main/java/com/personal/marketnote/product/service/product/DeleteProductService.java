package com.personal.marketnote.product.service.product;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.product.domain.product.Product;
import com.personal.marketnote.product.exception.NotProductOwnerException;
import com.personal.marketnote.product.port.in.command.DeleteProductCommand;
import com.personal.marketnote.product.port.in.usecase.product.DeleteProductUseCase;
import com.personal.marketnote.product.port.in.usecase.product.GetProductUseCase;
import com.personal.marketnote.product.port.out.event.PublishProductEventPort;
import com.personal.marketnote.product.port.out.product.FindProductPort;
import com.personal.marketnote.product.port.out.product.UpdateProductPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@Slf4j
@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED)
public class DeleteProductService implements DeleteProductUseCase {
    private final GetProductUseCase getProductUseCase;
    private final FindProductPort findProductPort;
    private final UpdateProductPort updateProductPort;
    private final PublishProductEventPort publishProductEventPort;

    @Override
    public void delete(Long userId, boolean isAdmin, DeleteProductCommand command) {
        Long id = command.id();
        if (!isAdmin && !findProductPort.existsByIdAndSellerId(id, userId)) {
            log.warn("상품 소유자가 아닙니다. productId={}", id);
            throw new NotProductOwnerException();
        }

        Product product = getProductUseCase.getProduct(id);
        product.delete();

        updateProductPort.update(product);
        publishProductEventPort.publishProductDeletedEvent(id);
    }
}
