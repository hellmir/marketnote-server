package com.personal.marketnote.product.service.product;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.kafka.event.ProductRegisteredEvent;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.product.domain.product.Product;
import com.personal.marketnote.product.domain.product.ProductTag;
import com.personal.marketnote.product.mapper.ProductCommandToStateMapper;
import com.personal.marketnote.product.port.in.command.FulfillmentVendorGoodsOptionCommand;
import com.personal.marketnote.product.port.in.command.RegisterPricePolicyCommand;
import com.personal.marketnote.product.port.in.command.RegisterProductCommand;
import com.personal.marketnote.product.port.in.result.pricepolicy.RegisterPricePolicyResult;
import com.personal.marketnote.product.port.in.result.product.RegisterProductResult;
import com.personal.marketnote.product.port.in.usecase.pricepolicy.RegisterPricePolicyUseCase;
import com.personal.marketnote.product.port.in.usecase.product.RegisterProductUseCase;
import com.personal.marketnote.product.port.out.event.PublishProductEventPort;
import com.personal.marketnote.product.port.out.product.SaveProductPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED)
public class RegisterProductService implements RegisterProductUseCase {
    private final RegisterPricePolicyUseCase registerPricePolicyUseCase;
    private final SaveProductPort saveProductPort;
    private final PublishProductEventPort publishProductEventPort;

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

        // Outbox 이벤트 저장 (트랜잭션 내)
        FulfillmentVendorGoodsOptionCommand fulfillmentOptions = command.fulfillmentVendorGoods();
        String goodsType = FormatValidator.hasValue(fulfillmentOptions) && FormatValidator.hasValue(fulfillmentOptions.goodsType())
                ? fulfillmentOptions.goodsType()
                : null;

        Long sales = FormatValidator.hasValue(savedProduct.getSales())
                ? savedProduct.getSales().longValue()
                : 0L;
        List<ProductRegisteredEvent.ProductTagSnapshot> productTags = toTagSnapshots(savedProduct.getProductTags());

        publishProductEventPort.publishProductRegisteredEvent(
                savedProduct.getId(), savedProduct.getProductKey(),
                registerPricePolicyResult.id(), command.sellerId(), savedProduct.getName(), goodsType,
                savedProduct.getBrandName(), command.price(), command.discountPrice(), command.accumulatedPoint(),
                sales, productTags, List.of()
        );

        return RegisterProductResult.from(savedProduct);
    }

    private List<ProductRegisteredEvent.ProductTagSnapshot> toTagSnapshots(List<ProductTag> productTags) {
        if (FormatValidator.hasNoValue(productTags)) {
            return List.of();
        }
        return productTags.stream()
                .map(tag -> new ProductRegisteredEvent.ProductTagSnapshot(
                        tag.getId(), tag.getName(), tag.getOrderNum()
                ))
                .toList();
    }
}
