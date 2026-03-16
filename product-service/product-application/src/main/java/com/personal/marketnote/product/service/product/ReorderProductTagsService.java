package com.personal.marketnote.product.service.product;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.product.domain.product.Product;
import com.personal.marketnote.product.domain.product.ProductTag;
import com.personal.marketnote.product.exception.DuplicateProductTagOrderException;
import com.personal.marketnote.product.exception.NotProductOwnerException;
import com.personal.marketnote.product.exception.ProductTagNotFoundException;
import com.personal.marketnote.product.port.in.command.ReorderProductTagsCommand;
import com.personal.marketnote.product.port.in.usecase.product.GetProductUseCase;
import com.personal.marketnote.product.port.in.usecase.product.ReorderProductTagsUseCase;
import com.personal.marketnote.product.port.out.product.FindProductPort;
import com.personal.marketnote.product.port.out.product.UpdateProductTagPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.personal.marketnote.common.domain.exception.ExceptionCode.FIRST_ERROR_CODE;
import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED)
public class ReorderProductTagsService implements ReorderProductTagsUseCase {
    private final GetProductUseCase getProductUseCase;
    private final FindProductPort findProductPort;
    private final UpdateProductTagPort updateProductTagPort;

    @Override
    public void reorderProductTags(Long userId, boolean isAdmin, ReorderProductTagsCommand command) {
        Long productId = command.productId();

        validateOwnership(userId, isAdmin, productId);

        Product product = getProductUseCase.getProduct(productId);

        validateNoDuplicates(command);
        validateTagsBelongToProduct(product, command);

        Map<Long, Long> tagIdToOrderNumMap = buildTagIdToOrderNumMap(command);
        updateProductTagPort.updateOrderNums(productId, tagIdToOrderNumMap);
    }

    private void validateOwnership(Long userId, boolean isAdmin, Long productId) {
        if (isAdmin) {
            return;
        }
        if (!findProductPort.existsByIdAndSellerId(productId, userId)) {
            throw new NotProductOwnerException(FIRST_ERROR_CODE, productId);
        }
    }

    private void validateNoDuplicates(ReorderProductTagsCommand command) {
        Set<Long> tagIds = new HashSet<>();
        Set<Long> orderNums = new HashSet<>();
        for (ReorderProductTagsCommand.TagOrderItem item : command.tagOrders()) {
            if (!tagIds.add(item.tagId())) {
                throw DuplicateProductTagOrderException.duplicateTagId(item.tagId());
            }
            if (!orderNums.add(item.orderNum())) {
                throw DuplicateProductTagOrderException.duplicateOrderNum(item.orderNum());
            }
        }
    }

    private void validateTagsBelongToProduct(Product product, ReorderProductTagsCommand command) {
        Set<Long> existingTagIds = product.getProductTags().stream()
                .map(ProductTag::getId)
                .collect(Collectors.toSet());

        for (ReorderProductTagsCommand.TagOrderItem item : command.tagOrders()) {
            if (!existingTagIds.contains(item.tagId())) {
                throw new ProductTagNotFoundException(item.tagId(), product.getId());
            }
        }
    }

    private Map<Long, Long> buildTagIdToOrderNumMap(ReorderProductTagsCommand command) {
        Map<Long, Long> map = new LinkedHashMap<>();
        for (ReorderProductTagsCommand.TagOrderItem item : command.tagOrders()) {
            map.put(item.tagId(), item.orderNum());
        }
        return map;
    }
}
