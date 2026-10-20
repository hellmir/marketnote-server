package com.personal.marketnote.product.adapter.out.persistence.pricepolicy;

import com.personal.marketnote.common.adapter.out.PersistenceAdapter;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.product.adapter.out.mapper.PricePolicyJpaEntityToDomainMapper;
import com.personal.marketnote.product.adapter.out.persistence.pricepolicy.entity.PricePolicyJpaEntity;
import com.personal.marketnote.product.adapter.out.persistence.pricepolicy.repository.PricePolicyJpaRepository;
import com.personal.marketnote.product.adapter.out.persistence.product.entity.ProductJpaEntity;
import com.personal.marketnote.product.adapter.out.persistence.product.repository.ProductJpaRepository;
import com.personal.marketnote.product.adapter.out.persistence.productoption.repository.ProductOptionPricePolicyJpaRepository;
import com.personal.marketnote.product.domain.pricepolicy.PricePolicy;
import com.personal.marketnote.product.domain.pricepolicy.PricePolicySnapshotState;
import com.personal.marketnote.product.domain.pricepolicy.Rate;
import com.personal.marketnote.product.domain.product.Product;
import com.personal.marketnote.product.domain.product.ProductSearchTarget;
import com.personal.marketnote.product.domain.product.ProductSnapshotState;
import com.personal.marketnote.product.domain.product.ProductSortProperty;
import com.personal.marketnote.product.exception.PricePolicyNotFoundException;
import com.personal.marketnote.product.port.out.pricepolicy.*;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@PersistenceAdapter
@RequiredArgsConstructor
public class PricePolicyPersistenceAdapter implements SavePricePolicyPort, FindPricePolicyPort, FindPricePoliciesPort, DeletePricePolicyPort, UpdatePopularityPort {
    private final ProductJpaRepository productJpaRepository;
    private final PricePolicyJpaRepository pricePolicyJpaRepository;
    private final ProductOptionPricePolicyJpaRepository productOptionPricePolicyJpaRepository;

    @Override
    @CacheEvict(value = {"product:detail", "product:price-policy"}, key = "#pricePolicy.product.id", beforeInvocation = false)
    public Long save(PricePolicy pricePolicy) {
        ProductJpaEntity productRef = productJpaRepository.getReferenceById(pricePolicy.getProduct().getId());
        PricePolicyJpaEntity savedPricePolicyJpaEntity
                = pricePolicyJpaRepository.save(PricePolicyJpaEntity.from(productRef, pricePolicy));
        savedPricePolicyJpaEntity.setIdToOrderNum();

        return savedPricePolicyJpaEntity.getId();
    }

    @Override
    public Optional<PricePolicy> findById(Long id) {
        return PricePolicyJpaEntityToDomainMapper.mapToDomain(pricePolicyJpaRepository.findById(id).orElse(null));
    }

    @Override
    public Optional<PricePolicy> findByProductAndOptionIds(Long productId, List<Long> optionIds) {
        if (FormatValidator.hasNoValue(optionIds)) {
            return Optional.empty();
        }

        List<Long> candidateIds = productOptionPricePolicyJpaRepository.findExactMatchingPricePolicyIds(optionIds, optionIds.size());
        if (FormatValidator.hasNoValue(candidateIds)) {
            return Optional.empty();
        }

        List<PricePolicyJpaEntity> candidates = pricePolicyJpaRepository.findAllById(candidateIds);

        PricePolicyJpaEntity entity = candidates.stream()
                .filter(p -> productId.equals(p.getProductJpaEntity().getId()))
                .max(Comparator.comparingLong(PricePolicyJpaEntity::getId))
                .orElse(null);

        if (FormatValidator.hasNoValue(entity)) {
            return Optional.empty();
        }

        ProductJpaEntity productJpaEntity = entity.getProductJpaEntity();
        Product product = Product.from(
                ProductSnapshotState.builder()
                        .id(productJpaEntity.getId())
                        .sellerId(productJpaEntity.getSellerId())
                        .name(productJpaEntity.getName())
                        .brandName(productJpaEntity.getBrandName())
                        .detail(productJpaEntity.getDetail())
                        .defaultPricePolicy(
                                PricePolicyJpaEntityToDomainMapper.mapToDomain(productJpaEntity.getDefaultPricePolicy())
                                        .orElse(null)
                        )
                        .sales(productJpaEntity.getSales())
                        .viewCount(productJpaEntity.getViewCount())
                        .popularity(productJpaEntity.getPopularity())
                        .findAllOptionsYn(productJpaEntity.isFindAllOptionsYn())
                        .productTags(java.util.List.of())
                        .orderNum(productJpaEntity.getOrderNum())
                        .status(productJpaEntity.getStatus())
                        .build()
        );

        PricePolicy pricePolicy = PricePolicy.from(
                PricePolicySnapshotState.builder()
                        .id(entity.getId())
                        .product(product)
                        .price(entity.getPrice())
                        .discountPrice(entity.getDiscountPrice())
                        .discountRate(Rate.of(entity.getDiscountRate()))
                        .accumulatedPoint(entity.getAccumulatedPoint())
                        .accumulationRate(Rate.of(entity.getAccumulationRate()))
                        .popularity(entity.getPopularity())
                        .status(entity.getStatus())
                        .orderNum(entity.getOrderNum())
                        .build()
        );

        return Optional.of(pricePolicy);
    }

    @Override
    public Optional<PricePolicy> findByOptionIds(List<Long> optionIds) {
        return pricePolicyJpaRepository.findOneByOptionIds(optionIds)
                .flatMap(PricePolicyJpaEntityToDomainMapper::mapToDomain);
    }

    @Override
    public List<PricePolicy> findByProductId(Long productId) {
        List<PricePolicyJpaEntity> policyEntities = loadPricePoliciesWithAssociations(
                pricePolicyJpaRepository.findAllByProductJpaEntity_IdOrderByIdDesc(productId)
        );

        Map<Long, List<Long>> optionIdsByPolicyId = fetchOptionIdsByPricePolicyIds(
                policyEntities.stream().map(PricePolicyJpaEntity::getId).toList()
        );

        return policyEntities.stream()
                .map(policyEntity -> PricePolicyJpaEntityToDomainMapper.mapToDomain(
                        policyEntity,
                        optionIdsByPolicyId.getOrDefault(policyEntity.getId(), List.of())
                ))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    @Override
    public List<PricePolicy> findByIds(List<Long> ids) {
        return loadPricePoliciesWithAssociationsByIds(ids).stream()
                .map(PricePolicyJpaEntityToDomainMapper::mapToDomainWithOptions)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    @Override
    public long countActivePricePoliciesByCategoryId(
            Long categoryId, ProductSearchTarget searchTarget, String searchKeyword
    ) {
        String searchPattern = generateSearchPattern(searchKeyword);

        if (FormatValidator.hasValue(categoryId)) {
            return pricePolicyJpaRepository.countActiveByCategoryId(
                    categoryId,
                    searchTarget.getCamelCaseValue(),
                    searchPattern
            );
        }

        return pricePolicyJpaRepository.countActive(
                searchTarget.getCamelCaseValue(),
                searchPattern
        );
    }

    @Override
    public List<PricePolicy> findPricePoliciesByOffset(
            List<Long> pricePolicyIds,
            int page,
            int pageSize,
            Sort.Direction sortDirection,
            ProductSortProperty sortProperty,
            ProductSearchTarget searchTarget,
            String searchKeyword,
            Long categoryId
    ) {
        boolean isAsc = sortDirection == Sort.Direction.ASC;

        List<PricePolicyJpaEntity> entities = pricePolicyJpaRepository.findActivePricePoliciesByOffset(
                pricePolicyIds,
                page,
                pageSize,
                isAsc,
                sortProperty,
                searchTarget,
                searchKeyword,
                categoryId
        );

        List<PricePolicyJpaEntity> hydrated = loadPricePoliciesWithAssociations(entities);
        Map<Long, List<Long>> optionIdsByPolicyId = fetchOptionIdsByPricePolicyIds(
                hydrated.stream().map(PricePolicyJpaEntity::getId).toList()
        );

        return new ArrayList<>(hydrated.stream()
                .map(policyEntity -> PricePolicyJpaEntityToDomainMapper.mapToDomain(
                        policyEntity,
                        optionIdsByPolicyId.getOrDefault(policyEntity.getId(), List.of())
                ))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList());
    }

    private Map<Long, List<Long>> fetchOptionIdsByPricePolicyIds(List<Long> pricePolicyIds) {
        if (FormatValidator.hasNoValue(pricePolicyIds)) {
            return Map.of();
        }
        return productOptionPricePolicyJpaRepository.findAllOptionIdsByPricePolicyIds(pricePolicyIds).stream()
                .collect(Collectors.groupingBy(
                        ProductOptionPricePolicyJpaRepository.PricePolicyOptionMapping::getPricePolicyId,
                        Collectors.mapping(
                                ProductOptionPricePolicyJpaRepository.PricePolicyOptionMapping::getProductOptionId,
                                Collectors.toList()
                        )
                ));
    }

    private String generateSearchPattern(String searchKeyword) {
        if (FormatValidator.hasValue(searchKeyword)) {
            return "%" + searchKeyword + "%";
        }

        return "";
    }

    @Override
    public void deleteById(Long pricePolicyId) {
        Long productId = pricePolicyJpaRepository.findById(pricePolicyId)
                .map(entity -> entity.getProductJpaEntity().getId())
                .orElseThrow(() -> new PricePolicyNotFoundException(pricePolicyId));
        deleteByIdInternal(productId, pricePolicyId);
    }

    @CacheEvict(value = {"product:detail", "product:price-policy"}, key = "#productId", beforeInvocation = false)
    public void deleteByIdInternal(Long productId, Long pricePolicyId) {
        productOptionPricePolicyJpaRepository.deleteByPricePolicyId(pricePolicyId);
        pricePolicyJpaRepository.findById(pricePolicyId).ifPresent(PricePolicyJpaEntity::deactivate);
    }

    @Override
    @CacheEvict(value = "pricePolicy:list:first", allEntries = true)
    public void updateWeeklyPopularity(LocalDateTime since) {
        pricePolicyJpaRepository.updateWeeklyPopularity(since);
    }

    private List<PricePolicyJpaEntity> loadPricePoliciesWithAssociations(List<PricePolicyJpaEntity> baseEntities) {
        if (FormatValidator.hasNoValue(baseEntities)) {
            return List.of();
        }

        List<Long> ids = baseEntities.stream()
                .map(PricePolicyJpaEntity::getId)
                .filter(Objects::nonNull)
                .toList();

        return loadPricePoliciesWithAssociationsByIds(ids);
    }

    private List<PricePolicyJpaEntity> loadPricePoliciesWithAssociationsByIds(List<Long> ids) {
        if (FormatValidator.hasNoValue(ids)) {
            return List.of();
        }

        List<PricePolicyJpaEntity> hydrated = pricePolicyJpaRepository.findAllWithProductAndOptionMappingsByIdIn(ids);
        if (FormatValidator.hasNoValue(hydrated)) {
            return List.of();
        }

        Map<Long, PricePolicyJpaEntity> hydratedById = new LinkedHashMap<>();
        for (PricePolicyJpaEntity entity : hydrated) {
            hydratedById.putIfAbsent(entity.getId(), entity);
        }

        return ids.stream()
                .map(hydratedById::get)
                .filter(Objects::nonNull)
                .toList();
    }
}
