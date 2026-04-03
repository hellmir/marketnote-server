package com.personal.marketnote.commerce.adapter.out.persistence.product;

import com.personal.marketnote.common.adapter.out.PersistenceAdapter;
import com.personal.marketnote.common.adapter.out.persistence.audit.EntityStatus;
import com.personal.marketnote.commerce.adapter.out.persistence.product.entity.ProductReadModelJpaEntity;
import com.personal.marketnote.commerce.adapter.out.persistence.product.repository.ProductReadModelJpaRepository;
import com.personal.marketnote.commerce.port.out.product.FindProductByPricePolicyPort;
import com.personal.marketnote.commerce.port.out.product.SaveProductReadModelPort;
import com.personal.marketnote.commerce.port.out.result.product.ProductInfoResult;
import com.personal.marketnote.common.utility.FormatValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@Slf4j
@PersistenceAdapter
@RequiredArgsConstructor
public class ProductReadModelPersistenceAdapter implements FindProductByPricePolicyPort, SaveProductReadModelPort {

    private final ProductReadModelJpaRepository productReadModelJpaRepository;

    @Override
    @Transactional(isolation = READ_COMMITTED, readOnly = true)
    public Map<Long, ProductInfoResult> findByPricePolicyIds(List<Long> pricePolicyIds) {
        if (FormatValidator.hasNoValue(pricePolicyIds)) {
            return Map.of();
        }

        List<ProductReadModelJpaEntity> entities =
                productReadModelJpaRepository.findByPricePolicyIdInAndStatus(pricePolicyIds, EntityStatus.ACTIVE);

        Map<Long, ProductInfoResult> resultMap = new HashMap<>();
        for (ProductReadModelJpaEntity entity : entities) {
            resultMap.put(
                    entity.getPricePolicyId(),
                    new ProductInfoResult(
                            entity.getProductId(),
                            entity.getSellerId(),
                            entity.getName(),
                            entity.getBrandName(),
                            entity.getPrice(),
                            entity.getDiscountPrice(),
                            entity.getAccumulatedPoint(),
                            List.of()
                    )
            );
        }

        return resultMap;
    }

    @Override
    @Transactional(isolation = READ_COMMITTED)
    public void upsert(Long pricePolicyId, Long productId, Long sellerId, String name, String brandName, Long price, Long discountPrice, Long accumulatedPoint) {
        Optional<ProductReadModelJpaEntity> existing =
                productReadModelJpaRepository.findByPricePolicyId(pricePolicyId);

        if (existing.isPresent()) {
            existing.get().updateFrom(name, brandName, price, discountPrice, accumulatedPoint);
            return;
        }

        try {
            ProductReadModelJpaEntity entity = ProductReadModelJpaEntity.of(
                    pricePolicyId, productId, sellerId, name, brandName, price, discountPrice, accumulatedPoint
            );
            productReadModelJpaRepository.saveAndFlush(entity);
        } catch (DataIntegrityViolationException e) {
            log.info("상품 Read Model 중복 저장 (멱등 처리). pricePolicyId={}", pricePolicyId);
            productReadModelJpaRepository.findByPricePolicyId(pricePolicyId)
                    .ifPresent(entity -> entity.updateFrom(name, brandName, price, discountPrice, accumulatedPoint));
        }
    }

    @Override
    @Transactional(isolation = READ_COMMITTED)
    public void deactivateByPricePolicyId(Long pricePolicyId) {
        productReadModelJpaRepository.findByPricePolicyId(pricePolicyId)
                .ifPresent(ProductReadModelJpaEntity::markInactive);
    }

    @Transactional(isolation = READ_COMMITTED)
    public void updateNameByProductId(Long productId, String name) {
        List<ProductReadModelJpaEntity> entities = productReadModelJpaRepository.findByProductId(productId);
        for (ProductReadModelJpaEntity entity : entities) {
            entity.updateName(name);
        }
    }
}
