package com.personal.marketnote.product.service.product;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.kafka.event.ProductTagChangedEvent;
import com.personal.marketnote.common.kafka.event.ProductUpdatedEvent;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.product.domain.product.Product;
import com.personal.marketnote.product.domain.product.ProductTag;
import com.personal.marketnote.product.exception.NotProductOwnerException;
import com.personal.marketnote.product.mapper.ProductUpdatedEventMapper;
import com.personal.marketnote.product.port.in.command.UpdateProductCommand;
import com.personal.marketnote.product.port.in.result.product.UpdateProductResult;
import com.personal.marketnote.product.port.in.usecase.product.GetProductUseCase;
import com.personal.marketnote.product.port.in.usecase.product.UpdateProductUseCase;
import com.personal.marketnote.product.port.out.event.PublishProductEventPort;
import com.personal.marketnote.product.port.out.product.FindProductPort;
import com.personal.marketnote.product.port.out.product.UpdateProductPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.personal.marketnote.common.domain.exception.ExceptionCode.FIRST_ERROR_CODE;
import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED)
public class UpdateProductService implements UpdateProductUseCase {
    private final GetProductUseCase getProductUseCase;
    private final FindProductPort findProductPort;
    private final UpdateProductPort updateProductPort;
    private final PublishProductEventPort publishProductEventPort;

    @Override
    public UpdateProductResult update(Long userId, boolean isAdmin, UpdateProductCommand command) {
        Long id = command.id();
        if (!isAdmin && !findProductPort.existsByIdAndSellerId(id, userId)) {
            throw new NotProductOwnerException(FIRST_ERROR_CODE, id);
        }

        Product product = getProductUseCase.getProduct(id);
        product.update(
                command.name(), command.brandName(), command.detail(),
                Boolean.TRUE.equals(command.isFindAllOptions()), command.tags()
        );

        updateProductPort.update(product);

        if (FormatValidator.hasValue(command.fulfillmentVendorGoods())) {
            ProductUpdatedEvent productUpdatedEvent =
                    ProductUpdatedEventMapper.mapToEvent(product, command.fulfillmentVendorGoods());

            publishProductEventPort.publishProductUpdatedEvent(productUpdatedEvent);
        }

        publishProductTagChangedEvent(id);

        return UpdateProductResult.from(product);
    }

    private void publishProductTagChangedEvent(Long productId) {
        Product refreshed = findProductPort.findById(productId).orElse(null);
        if (FormatValidator.hasNoValue(refreshed)) {
            return;
        }
        List<ProductTagChangedEvent.ProductTagSnapshot> productTags = toTagSnapshots(refreshed.getProductTags());
        publishProductEventPort.publishProductTagChangedEvent(
                productId, productTags, ProductTagChangedEvent.Action.UPDATED
        );
    }

    private List<ProductTagChangedEvent.ProductTagSnapshot> toTagSnapshots(List<ProductTag> productTags) {
        if (FormatValidator.hasNoValue(productTags)) {
            return List.of();
        }
        return productTags.stream()
                .map(tag -> new ProductTagChangedEvent.ProductTagSnapshot(
                        tag.getId(), tag.getName(), tag.getOrderNum()
                ))
                .toList();
    }
}
