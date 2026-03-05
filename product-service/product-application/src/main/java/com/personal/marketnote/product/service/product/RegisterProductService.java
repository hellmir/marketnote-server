package com.personal.marketnote.product.service.product;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.product.domain.product.Product;
import com.personal.marketnote.product.mapper.FulfillmentVendorGoodsCommandMapper;
import com.personal.marketnote.product.mapper.ProductCommandToStateMapper;
import com.personal.marketnote.product.port.in.command.FulfillmentVendorGoodsOptionCommand;
import com.personal.marketnote.product.port.in.command.RegisterPricePolicyCommand;
import com.personal.marketnote.product.port.in.command.RegisterProductCommand;
import com.personal.marketnote.product.port.in.result.pricepolicy.RegisterPricePolicyResult;
import com.personal.marketnote.product.port.in.result.product.RegisterProductResult;
import com.personal.marketnote.product.port.in.usecase.pricepolicy.RegisterPricePolicyUseCase;
import com.personal.marketnote.product.port.in.usecase.product.RegisterProductUseCase;
import com.personal.marketnote.product.port.out.event.PublishProductEventPort;
import com.personal.marketnote.product.port.out.fulfillment.RegisterFulfillmentVendorGoodsPort;
import com.personal.marketnote.product.port.out.inventory.RegisterInventoryPort;
import com.personal.marketnote.product.port.out.product.SaveProductPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED)
public class RegisterProductService implements RegisterProductUseCase {
    private static final String DEFAULT_GOD_TYPE = "1";

    private final RegisterPricePolicyUseCase registerPricePolicyUseCase;
    private final SaveProductPort saveProductPort;
    private final PublishProductEventPort publishProductEventPort;
    private final RegisterInventoryPort registerInventoryPort;
    private final RegisterFulfillmentVendorGoodsPort registerFulfillmentVendorGoodsPort;

    @Override
    public RegisterProductResult registerProduct(RegisterProductCommand command) {
        Long sellerId = command.sellerId();

        Product savedProduct = saveProductPort.save(
                Product.from(ProductCommandToStateMapper.mapToState(command))
        );

        Long productId = savedProduct.getId();
        RegisterPricePolicyResult registerPricePolicyResult = registerPricePolicyUseCase.registerPricePolicy(
                sellerId, false, RegisterPricePolicyCommand.from(productId, command)
        );

        // 트랜잭션이 있는 경우 트랜잭션 커밋 후 외부 시스템 요청
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    registerExternalSystems(savedProduct, command, registerPricePolicyResult.id());
                }
            });

            return RegisterProductResult.from(savedProduct);

        }

        // 트랜잭션이 없는 경우 직접 호출
        registerExternalSystems(savedProduct, command, registerPricePolicyResult.id());

        return RegisterProductResult.from(savedProduct);
    }

    private void registerExternalSystems(
            Product savedProduct,
            RegisterProductCommand command,
            Long pricePolicyId
    ) {
        // Kafka 이벤트 발행 (비동기)
        FulfillmentVendorGoodsOptionCommand fulfillmentOptions = command.fulfillmentVendorGoods();
        String godType = FormatValidator.hasValue(fulfillmentOptions) && FormatValidator.hasValue(fulfillmentOptions.godType())
                ? fulfillmentOptions.godType()
                : DEFAULT_GOD_TYPE;

        publishProductEventPort.publishProductRegisteredEvent(
                savedProduct.getId(), pricePolicyId, command.sellerId(), savedProduct.getName(), godType
        );

        // TODO: Kafka 검증 완료 후 HTTP 호출 제거
        registerInventoryPort.registerInventory(savedProduct.getId(), pricePolicyId);

        // TODO: Kafka 검증 완료 후 HTTP 호출 제거 (#934)
        registerFulfillmentVendorGoodsPort.registerFulfillmentVendorGoods(
                FulfillmentVendorGoodsCommandMapper.mapToRegisterCommand(savedProduct, command.fulfillmentVendorGoods())
        );
    }
}
