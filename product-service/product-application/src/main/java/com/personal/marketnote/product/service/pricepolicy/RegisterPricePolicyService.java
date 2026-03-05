package com.personal.marketnote.product.service.pricepolicy;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.product.domain.pricepolicy.PricePolicy;
import com.personal.marketnote.product.domain.product.Product;
import com.personal.marketnote.product.exception.NotProductOwnerException;
import com.personal.marketnote.product.mapper.ProductCommandToStateMapper;
import com.personal.marketnote.product.port.in.command.RegisterPricePolicyCommand;
import com.personal.marketnote.product.port.in.result.pricepolicy.RegisterPricePolicyResult;
import com.personal.marketnote.product.port.in.usecase.pricepolicy.RegisterPricePolicyUseCase;
import com.personal.marketnote.product.port.in.usecase.product.GetProductUseCase;
import com.personal.marketnote.product.port.out.event.PublishProductEventPort;
import com.personal.marketnote.product.port.out.inventory.RegisterInventoryPort;
import com.personal.marketnote.product.port.out.pricepolicy.SavePricePolicyPort;
import com.personal.marketnote.product.port.out.product.FindProductPort;
import com.personal.marketnote.product.port.out.productoption.UpdateOptionPricePolicyPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

import static com.personal.marketnote.common.domain.exception.ExceptionCode.FIRST_ERROR_CODE;
import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED)
public class RegisterPricePolicyService implements RegisterPricePolicyUseCase {
    private final GetProductUseCase getProductUseCase;
    private final FindProductPort findProductPort;
    private final SavePricePolicyPort savePricePolicyPort;
    private final UpdateOptionPricePolicyPort updateOptionPricePolicyPort;
    private final PublishProductEventPort publishProductEventPort;
    private final RegisterInventoryPort registerInventoryPort;

    @Override
    public RegisterPricePolicyResult registerPricePolicy(
            Long userId, boolean isAdmin, RegisterPricePolicyCommand command
    ) {
        Long productId = command.productId();
        if (!isAdmin && !findProductPort.existsByIdAndSellerId(productId, userId)) {
            throw new NotProductOwnerException(FIRST_ERROR_CODE, productId);
        }

        Product product = getProductUseCase.getProduct(productId);

        PricePolicy pricePolicy = PricePolicy.from(ProductCommandToStateMapper.mapToState(product, command));
        Long id = savePricePolicyPort.save(pricePolicy);

        List<Long> optionIds = command.optionIds();
        if (FormatValidator.hasValue(optionIds)) {
            updateOptionPricePolicyPort.assignPricePolicyToOptions(productId, id, optionIds);
        }

        registerInventoryAfterCommit(productId, id);

        return RegisterPricePolicyResult.of(id);
    }

    private void registerInventoryAfterCommit(Long productId, Long pricePolicyId) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    publishProductEventPort.publishPricePolicyCreatedEvent(productId, pricePolicyId);

                    // TODO: Kafka 검증 완료 후 HTTP 호출 제거 (#1017)
                    registerInventoryPort.registerInventory(productId, pricePolicyId);
                }
            });

            return;
        }

        publishProductEventPort.publishPricePolicyCreatedEvent(productId, pricePolicyId);

        // TODO: Kafka 검증 완료 후 HTTP 호출 제거 (#1017)
        registerInventoryPort.registerInventory(productId, pricePolicyId);
    }
}
