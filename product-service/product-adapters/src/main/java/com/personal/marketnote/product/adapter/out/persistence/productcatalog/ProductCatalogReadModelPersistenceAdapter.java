package com.personal.marketnote.product.adapter.out.persistence.productcatalog;

import com.personal.marketnote.common.adapter.out.PersistenceAdapter;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.product.adapter.out.persistence.productcatalog.entity.ProductCatalogReadModelJpaEntity;
import com.personal.marketnote.product.adapter.out.persistence.productcatalog.mapper.ProductCatalogReadModelJpaEntityToResultMapper;
import com.personal.marketnote.product.adapter.out.persistence.productcatalog.repository.ProductCatalogReadModelJpaRepository;
import com.personal.marketnote.product.domain.product.ProductSearchTarget;
import com.personal.marketnote.product.domain.product.ProductSortProperty;
import com.personal.marketnote.product.port.in.result.product.ProductCatalogItemResult;
import com.personal.marketnote.product.port.out.productcatalog.FindProductCatalogReadModelPort;
import com.personal.marketnote.product.port.out.productcatalog.SaveProductCatalogReadModelPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@Slf4j
@PersistenceAdapter
@RequiredArgsConstructor
public class ProductCatalogReadModelPersistenceAdapter
        implements FindProductCatalogReadModelPort, SaveProductCatalogReadModelPort {

    private final ProductCatalogReadModelJpaRepository repository;

    @Override
    @Transactional(isolation = READ_COMMITTED, readOnly = true)
    public List<ProductCatalogItemResult> findCatalogItems(
            Long categoryId,
            List<Long> pricePolicyIds,
            Long cursor,
            int pageSize,
            boolean isAsc,
            ProductSortProperty sortProperty,
            ProductSearchTarget searchTarget,
            String searchKeyword
    ) {
        List<ProductCatalogReadModelJpaEntity> entities = repository.findCatalogItems(
                categoryId, pricePolicyIds, cursor, pageSize, isAsc, sortProperty, searchTarget, searchKeyword
        );
        return entities.stream()
                .map(ProductCatalogReadModelJpaEntityToResultMapper::mapToResult)
                .toList();
    }

    @Override
    @Transactional(isolation = READ_COMMITTED, readOnly = true)
    public long countCatalogItems(Long categoryId, ProductSearchTarget searchTarget, String searchKeyword) {
        return repository.countCatalogItems(categoryId, searchTarget, searchKeyword);
    }

    @Override
    @Transactional(isolation = READ_COMMITTED)
    public void upsertCatalog(
            Long pricePolicyId,
            Long productId,
            Long sellerId,
            Long primaryCategoryId,
            String name,
            String brandName,
            Long price,
            Long discountPrice,
            Long accumulatedPoint,
            BigDecimal accumulatedPointRate,
            Long orderNum,
            Long popularity
    ) {
        Optional<ProductCatalogReadModelJpaEntity> existing = repository.findByPricePolicyId(pricePolicyId);

        if (existing.isPresent()) {
            ProductCatalogReadModelJpaEntity entity = existing.get();
            entity.updateProductInfo(name, brandName, primaryCategoryId);
            entity.updatePricing(price, discountPrice, accumulatedPoint, accumulatedPointRate);
            entity.updateOrderNum(orderNum);
            entity.updatePopularity(popularity);
            return;
        }

        try {
            ProductCatalogReadModelJpaEntity entity = ProductCatalogReadModelJpaEntity.of(
                    pricePolicyId, productId, sellerId, primaryCategoryId,
                    name, brandName, price, discountPrice, accumulatedPoint, accumulatedPointRate,
                    orderNum, popularity
            );
            repository.saveAndFlush(entity);
        } catch (DataIntegrityViolationException e) {
            ProductCatalogReadModelJpaEntity entity = repository.findByPricePolicyId(pricePolicyId)
                    .orElseThrow(() -> e);
            log.info("상품 카탈로그 Read Model 중복 저장 (멱등 처리). pricePolicyId={}", pricePolicyId);
            entity.updateProductInfo(name, brandName, primaryCategoryId);
            entity.updatePricing(price, discountPrice, accumulatedPoint, accumulatedPointRate);
            entity.updateOrderNum(orderNum);
            entity.updatePopularity(popularity);
        }
    }

    @Override
    @Transactional(isolation = READ_COMMITTED)
    public void updateStockQuantity(Long pricePolicyId, Integer stockQuantity) {
        repository.findByPricePolicyId(pricePolicyId)
                .ifPresent(entity -> entity.updateStockQuantity(stockQuantity));
    }

    @Override
    @Transactional(isolation = READ_COMMITTED)
    public void updateRepresentativeImageUrl(Long productId, String representativeImageUrl) {
        List<ProductCatalogReadModelJpaEntity> entities = repository.findAllByProductId(productId);
        if (FormatValidator.hasNoValue(entities)) {
            return;
        }
        entities.forEach(entity -> entity.updateRepresentativeImageUrl(representativeImageUrl));
    }

    @Override
    @Transactional(isolation = READ_COMMITTED)
    public void updateReviewAggregate(Long productId, Integer reviewCount, Float reviewAverageRating) {
        List<ProductCatalogReadModelJpaEntity> entities = repository.findAllByProductId(productId);
        if (FormatValidator.hasNoValue(entities)) {
            return;
        }
        entities.forEach(entity -> entity.updateReviewAggregate(reviewCount, reviewAverageRating));
    }

    @Override
    @Transactional(isolation = READ_COMMITTED)
    public void deleteByPricePolicyId(Long pricePolicyId) {
        repository.findByPricePolicyId(pricePolicyId)
                .ifPresent(entity -> repository.delete(entity));
    }

    @Override
    @Transactional(isolation = READ_COMMITTED)
    public void deleteByProductId(Long productId) {
        repository.deleteByProductId(productId);
    }
}
