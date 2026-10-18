package com.personal.marketnote.product.adapter.out.persistence.productcatalog;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final ObjectMapper objectMapper;

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
                .map(entity -> ProductCatalogReadModelJpaEntityToResultMapper.mapToResult(entity, objectMapper))
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
            BigDecimal discountRate,
            Long accumulatedPoint,
            BigDecimal accumulatedPointRate,
            Long orderNum,
            Long popularity,
            Long sales,
            String productTagsJson,
            String selectedOptionsJson
    ) {
        Long safeSales = FormatValidator.hasValue(sales) ? sales : 0L;
        Optional<ProductCatalogReadModelJpaEntity> existing = repository.findByPricePolicyId(pricePolicyId);

        if (existing.isPresent()) {
            applyUpdates(
                    existing.get(), name, brandName, primaryCategoryId, price, discountPrice, discountRate,
                    accumulatedPoint, accumulatedPointRate, orderNum, popularity,
                    safeSales, productTagsJson, selectedOptionsJson
            );
            return;
        }

        try {
            ProductCatalogReadModelJpaEntity entity = ProductCatalogReadModelJpaEntity.of(
                    pricePolicyId, productId, sellerId, primaryCategoryId,
                    name, brandName, price, discountPrice, discountRate,
                    accumulatedPoint, accumulatedPointRate,
                    orderNum, popularity,
                    safeSales, productTagsJson, selectedOptionsJson
            );
            repository.saveAndFlush(entity);
        } catch (DataIntegrityViolationException e) {
            ProductCatalogReadModelJpaEntity entity = repository.findByPricePolicyId(pricePolicyId)
                    .orElseThrow(() -> e);
            log.info("상품 카탈로그 Read Model 중복 저장 (멱등 처리). pricePolicyId={}", pricePolicyId);
            applyUpdates(
                    entity, name, brandName, primaryCategoryId, price, discountPrice, discountRate,
                    accumulatedPoint, accumulatedPointRate, orderNum, popularity,
                    safeSales, productTagsJson, selectedOptionsJson
            );
        }
    }

    private void applyUpdates(
            ProductCatalogReadModelJpaEntity entity,
            String name, String brandName, Long primaryCategoryId,
            Long price, Long discountPrice, BigDecimal discountRate,
            Long accumulatedPoint, BigDecimal accumulatedPointRate,
            Long orderNum, Long popularity,
            Long safeSales, String productTagsJson, String selectedOptionsJson
    ) {
        entity.updateProductInfo(name, brandName, primaryCategoryId);
        entity.updatePricing(price, discountPrice, discountRate, accumulatedPoint, accumulatedPointRate);
        entity.updateOrderNum(orderNum);
        entity.updatePopularity(popularity);
        entity.updateSales(safeSales);
        entity.updateProductTags(productTagsJson);
        entity.updateSelectedOptions(selectedOptionsJson);
    }

    @Override
    @Transactional(isolation = READ_COMMITTED)
    public void increaseSales(Long pricePolicyId, Long delta) {
        if (FormatValidator.hasNoValue(delta) || delta <= 0L) {
            return;
        }
        Optional<ProductCatalogReadModelJpaEntity> entity = repository.findByPricePolicyId(pricePolicyId);
        if (entity.isEmpty()) {
            log.warn("상품 카탈로그 Read Model 미존재 — 판매량 증가 누락. pricePolicyId={}, delta={}", pricePolicyId, delta);
            return;
        }
        entity.get().increaseSales(delta);
    }

    @Override
    @Transactional(isolation = READ_COMMITTED)
    public void updateProductTags(Long productId, String productTagsJson) {
        List<ProductCatalogReadModelJpaEntity> entities = repository.findAllByProductId(productId);
        if (FormatValidator.hasNoValue(entities)) {
            log.warn("상품 카탈로그 Read Model 미존재 — 태그 동기화 누락. productId={}", productId);
            return;
        }
        entities.forEach(entity -> entity.updateProductTags(productTagsJson));
    }

    @Override
    @Transactional(isolation = READ_COMMITTED)
    public void updateStockQuantity(Long pricePolicyId, Integer stockQuantity) {
        repository.findByPricePolicyId(pricePolicyId)
                .ifPresent(entity -> entity.updateStockQuantity(stockQuantity));
    }

    @Override
    @Transactional(isolation = READ_COMMITTED)
    public void updateRepresentativeImage(Long productId, String representativeImageUrl, List<String> resizedImageUrls) {
        List<ProductCatalogReadModelJpaEntity> entities = repository.findAllByProductId(productId);
        if (FormatValidator.hasNoValue(entities)) {
            return;
        }
        String serializedResized = serializeResizedImageUrls(resizedImageUrls);
        entities.forEach(entity -> entity.updateRepresentativeImage(representativeImageUrl, serializedResized));
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

    private String serializeResizedImageUrls(List<String> resizedImageUrls) {
        if (FormatValidator.hasNoValue(resizedImageUrls)) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(resizedImageUrls);
        } catch (JsonProcessingException jpe) {
            log.warn("상품 카탈로그 Read Model resized 이미지 직렬화 실패. urls={}", resizedImageUrls, jpe);
            return null;
        }
    }
}
