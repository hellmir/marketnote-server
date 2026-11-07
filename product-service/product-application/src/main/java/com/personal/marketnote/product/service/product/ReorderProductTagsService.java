package com.personal.marketnote.product.service.product;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.kafka.event.ProductTagChangedEvent;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.product.domain.product.Product;
import com.personal.marketnote.product.domain.product.ProductTag;
import com.personal.marketnote.product.exception.DuplicateProductTagOrderException;
import com.personal.marketnote.product.exception.NotProductOwnerException;
import com.personal.marketnote.product.exception.ProductTagNotFoundException;
import com.personal.marketnote.product.port.in.command.ReorderProductTagsCommand;
import com.personal.marketnote.product.port.in.usecase.product.GetProductUseCase;
import com.personal.marketnote.product.port.in.usecase.product.ReorderProductTagsUseCase;
import com.personal.marketnote.product.port.out.event.PublishProductEventPort;
import com.personal.marketnote.product.port.out.product.FindProductPort;
import com.personal.marketnote.product.port.out.product.UpdateProductTagPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@Slf4j
@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED)
public class ReorderProductTagsService implements ReorderProductTagsUseCase {
    private final GetProductUseCase getProductUseCase;
    private final FindProductPort findProductPort;
    private final UpdateProductTagPort updateProductTagPort;
    private final PublishProductEventPort publishProductEventPort;

    @Override
    public void reorderProductTags(Long userId, boolean isAdmin, ReorderProductTagsCommand command) {
        Long productId = command.productId();

        validateOwnership(userId, isAdmin, productId);

        Product product = getProductUseCase.getProduct(productId);

        validateNoDuplicates(command);
        validateTagsBelongToProduct(product, command);

        Map<Long, Long> tagIdToOrderNumMap = buildTagIdToOrderNumMap(command);
        updateProductTagPort.updateOrderNums(productId, tagIdToOrderNumMap);

        publishProductEventPort.publishProductTagChangedEvent(
                productId,
                toReorderedSnapshots(product.getProductTags(), tagIdToOrderNumMap),
                ProductTagChangedEvent.Action.REORDERED
        );
    }

    private void validateOwnership(Long userId, boolean isAdmin, Long productId) {
        if (isAdmin) {
            return;
        }
        if (!findProductPort.existsByIdAndSellerId(productId, userId)) {
            log.warn("상품 소유자가 아닙니다. productId={}", productId);
            throw new NotProductOwnerException();
        }
    }

    private void validateNoDuplicates(ReorderProductTagsCommand command) {
        Set<Long> tagIds = new HashSet<>();
        Set<Long> orderNums = new HashSet<>();
        for (ReorderProductTagsCommand.TagOrderItem item : command.tagOrders()) {
            if (!tagIds.add(item.tagId())) {
                log.warn("중복된 태그 ID가 존재합니다. tagId={}", item.tagId());
                throw DuplicateProductTagOrderException.duplicateTagId();
            }
            if (!orderNums.add(item.orderNum())) {
                log.warn("중복된 순서 번호가 존재합니다. orderNum={}", item.orderNum());
                throw DuplicateProductTagOrderException.duplicateOrderNum();
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

    private List<ProductTagChangedEvent.ProductTagSnapshot> toReorderedSnapshots(
            List<ProductTag> existingTags, Map<Long, Long> tagIdToOrderNumMap
    ) {
        if (FormatValidator.hasNoValue(existingTags)) {
            return List.of();
        }
        return existingTags.stream()
                .map(tag -> new ProductTagChangedEvent.ProductTagSnapshot(
                        tag.getId(),
                        tag.getName(),
                        tagIdToOrderNumMap.getOrDefault(tag.getId(), tag.getOrderNum())
                ))
                .toList();
    }
}
