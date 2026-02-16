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
import com.personal.marketnote.product.domain.product.Product;
import com.personal.marketnote.product.domain.product.ProductSearchTarget;
import com.personal.marketnote.product.domain.product.ProductSnapshotState;
import com.personal.marketnote.product.domain.product.ProductSortProperty;
import com.personal.marketnote.product.port.out.pricepolicy.DeletePricePolicyPort;
import com.personal.marketnote.product.port.out.pricepolicy.FindPricePoliciesPort;
import com.personal.marketnote.product.port.out.pricepolicy.FindPricePolicyPort;
import com.personal.marketnote.product.port.out.pricepolicy.SavePricePolicyPort;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.*;

@PersistenceAdapter
@RequiredArgsConstructor
public class PricePolicyPersistenceAdapter implements SavePricePolicyPort, FindPricePolicyPort, FindPricePoliciesPort, DeletePricePolicyPort {
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

        List<Long> candidateIds = productOptionPricePolicyJpaRepository.findCandidatePricePolicyIds(optionIds, optionIds.size());
        if (FormatValidator.hasNoValue(candidateIds)) {
            return Optional.empty();
        }

        Long matchedId = null;
        for (Long id : candidateIds) {
            long mappingCount = productOptionPricePolicyJpaRepository.countByPricePolicyJpaEntity_Id(id);
            if (mappingCount == optionIds.size()) {
                Optional<PricePolicyJpaEntity> policyOpt = pricePolicyJpaRepository.findById(id);

                if (policyOpt.isPresent() && policyOpt.get().getProductJpaEntity().getId().equals(productId)) {
                    if (FormatValidator.hasNoValue(matchedId) || id > matchedId) {
                        matchedId = id;
                    }
                }
            }
        }

        if (FormatValidator.hasNoValue(matchedId)) {
            return Optional.empty();
        }

        PricePolicyJpaEntity entity = pricePolicyJpaRepository.findById(matchedId)
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
                        .discountRate(entity.getDiscountRate())
                        .accumulatedPoint(entity.getAccumulatedPoint())
                        .accumulationRate(entity.getAccumulationRate())
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

        return policyEntities.stream()
                .map(policyEntity -> PricePolicyJpaEntityToDomainMapper.mapToDomain(
                        policyEntity, productOptionPricePolicyJpaRepository.findOptionIdsByPricePolicyId(policyEntity.getId())
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
    @Cacheable(
            value = "pricePolicy:list:first",
            key = "#pageable.getPageSize() + ':' + #sortProperty.name() + ':' + #searchTarget.name()",
            condition = "#cursor == null && (#searchKeyword == null || #searchKeyword.isBlank())"
    )
    public List<PricePolicy> findPricePolicies(
            List<Long> pricePolicyIds,
            Long cursor,
            Pageable pageable,
            ProductSortProperty sortProperty,
            ProductSearchTarget searchTarget,
            String searchKeyword
    ) {
        boolean isAsc = pageable.getSort()
                .stream()
                .findFirst()
                .map(Sort.Order::isAscending)
                .orElse(true);

        String pattern = generateSearchPattern(searchKeyword);

        List<PricePolicyJpaEntity> entities = isAsc
                ? pricePolicyJpaRepository.findAllActiveByCursorAsc(
                pricePolicyIds,
                cursor,
                pageable,
                sortProperty.getCamelCaseValue(),
                searchTarget.getCamelCaseValue(),
                pattern,
                null
        )
                : pricePolicyJpaRepository.findAllActiveByCursorDesc(
                pricePolicyIds,
                cursor,
                pageable,
                sortProperty.getCamelCaseValue(),
                searchTarget.getCamelCaseValue(),
                pattern,
                null
        );

        return loadPricePoliciesWithAssociations(entities).stream()
                .map(policyEntity -> PricePolicyJpaEntityToDomainMapper.mapToDomain(
                        policyEntity, productOptionPricePolicyJpaRepository.findOptionIdsByPricePolicyId(policyEntity.getId())
                ))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    @Override
    @Cacheable(
            value = "pricePolicy:list:first",
            key = "#categoryId + ':' + #pageable.getPageSize() + ':' + #sortProperty.name() + ':' + #searchTarget.name()",
            condition = "#cursor == null && (#searchKeyword == null || #searchKeyword.isBlank())"
    )
    public List<PricePolicy> findPricePoliciesByCategoryId(
            Long categoryId,
            List<Long> pricePolicyIds,
            Long cursor,
            Pageable pageable,
            ProductSortProperty sortProperty,
            ProductSearchTarget searchTarget,
            String searchKeyword
    ) {
        boolean isAsc = pageable.getSort()
                .stream()
                .findFirst()
                .map(Sort.Order::isAscending)
                .orElse(true);

        String pattern = generateSearchPattern(searchKeyword);

        List<PricePolicyJpaEntity> entities = isAsc
                ? pricePolicyJpaRepository.findAllActiveByCursorAsc(
                pricePolicyIds,
                cursor,
                pageable,
                sortProperty.getCamelCaseValue(),
                searchTarget.getCamelCaseValue(),
                pattern,
                categoryId
        )
                : pricePolicyJpaRepository.findAllActiveByCursorDesc(
                pricePolicyIds,
                cursor,
                pageable,
                sortProperty.getCamelCaseValue(),
                searchTarget.getCamelCaseValue(),
                pattern,
                categoryId
        );

        return loadPricePoliciesWithAssociations(entities).stream()
                .map(policyEntity -> PricePolicyJpaEntityToDomainMapper.mapToDomain(
                        policyEntity, productOptionPricePolicyJpaRepository.findOptionIdsByPricePolicyId(policyEntity.getId())
                ))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    @Override
    public long countActivePricePoliciesByCategoryId(Long categoryId, ProductSearchTarget searchTarget, String searchKeyword) {
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

    private String generateSearchPattern(String searchKeyword) {
        if (FormatValidator.hasValue(searchKeyword)) {
            return "%" + searchKeyword + "%";
        }

        return null;
    }

    @Override
    public void deleteById(Long pricePolicyId) {
        Long productId = pricePolicyJpaRepository.findById(pricePolicyId)
                .map(entity -> entity.getProductJpaEntity().getId())
                .orElse(null);
        deleteByIdInternal(productId, pricePolicyId);
    }

    @CacheEvict(value = {"product:detail", "product:price-policy"}, key = "#productId", beforeInvocation = false)
    public void deleteByIdInternal(Long productId, Long pricePolicyId) {
        productOptionPricePolicyJpaRepository.deleteByPricePolicyId(pricePolicyId);
        pricePolicyJpaRepository.deleteById(pricePolicyId);
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
