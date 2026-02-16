package com.personal.marketnote.product.adapter.out.persistence.productoption;

import com.personal.marketnote.common.adapter.out.PersistenceAdapter;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.product.adapter.out.mapper.ProductJpaEntityToDomainMapper;
import com.personal.marketnote.product.adapter.out.persistence.pricepolicy.entity.PricePolicyJpaEntity;
import com.personal.marketnote.product.adapter.out.persistence.pricepolicy.repository.PricePolicyJpaRepository;
import com.personal.marketnote.product.adapter.out.persistence.product.entity.ProductJpaEntity;
import com.personal.marketnote.product.adapter.out.persistence.product.repository.ProductJpaRepository;
import com.personal.marketnote.product.adapter.out.persistence.productoption.entity.ProductOptionCategoryJpaEntity;
import com.personal.marketnote.product.adapter.out.persistence.productoption.entity.ProductOptionJpaEntity;
import com.personal.marketnote.product.adapter.out.persistence.productoption.entity.ProductOptionPricePolicyJpaEntity;
import com.personal.marketnote.product.adapter.out.persistence.productoption.repository.ProductOptionCategoryJpaRepository;
import com.personal.marketnote.product.adapter.out.persistence.productoption.repository.ProductOptionJpaRepository;
import com.personal.marketnote.product.adapter.out.persistence.productoption.repository.ProductOptionPricePolicyJpaRepository;
import com.personal.marketnote.product.domain.option.ProductOptionCategory;
import com.personal.marketnote.product.domain.pricepolicy.PricePolicy;
import com.personal.marketnote.product.domain.pricepolicy.PricePolicyCreateState;
import com.personal.marketnote.product.exception.ProductNoPricePolicyException;
import com.personal.marketnote.product.exception.ProductNotFoundException;
import com.personal.marketnote.product.port.out.productoption.DeleteProductOptionCategoryPort;
import com.personal.marketnote.product.port.out.productoption.FindProductOptionCategoryPort;
import com.personal.marketnote.product.port.out.productoption.SaveProductOptionsPort;
import com.personal.marketnote.product.port.out.productoption.UpdateOptionPricePolicyPort;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@PersistenceAdapter
@RequiredArgsConstructor
public class ProductOptionPersistenceAdapter implements SaveProductOptionsPort, DeleteProductOptionCategoryPort, FindProductOptionCategoryPort, UpdateOptionPricePolicyPort {
    private final ProductJpaRepository productJpaRepository;
    private final ProductOptionCategoryJpaRepository productOptionCategoryJpaRepository;
    private final ProductOptionJpaRepository productOptionJpaRepository;
    private final ProductOptionPricePolicyJpaRepository productOptionPricePolicyJpaRepository;
    private final PricePolicyJpaRepository pricePolicyJpaRepository;

    @Override
    @CacheEvict(value = "product:detail", key = "#productOptionCategory.product.id")
    public ProductOptionCategory save(ProductOptionCategory productOptionCategory) {
        Long productId = productOptionCategory.getProduct().getId();
        ProductJpaEntity product = productJpaRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        ProductOptionCategoryJpaEntity savedCategory = productOptionCategoryJpaRepository.save(
                ProductOptionCategoryJpaEntity.from(productOptionCategory, product));
        savedCategory.addOrderNum();

        // 생성된 각 옵션 및 기존 옵션과의 조합에 대해 기본 가격 정책 등록
        // region

        // 상품의 모든 활성 카테고리와 옵션 가져오기
        List<ProductOptionCategoryJpaEntity> allCategories = productOptionCategoryJpaRepository
                .findActiveWithOptionsByProductId(productId);
        List<PricePolicyJpaEntity> newPolicies = new ArrayList<>();
        List<ProductOptionPricePolicyJpaEntity> newOptionPolicies = new ArrayList<>();
        registerPricePoliciesToOptionCombinations(allCategories, savedCategory, newPolicies, newOptionPolicies, product);

        // 기존 가격 정책 비활성화 및 옵션-가격 정책 매핑 삭제 (장바구니 참조 보호)
        List<PricePolicyJpaEntity> existingPolicies
                = pricePolicyJpaRepository.findAllByProductJpaEntity_IdOrderByIdDesc(productId);
        if (FormatValidator.hasNoValue(existingPolicies)) {
            existingPolicies = List.of();
        }
        List<Long> existingPolicyIds = existingPolicies.stream()
                .map(PricePolicyJpaEntity::getId)
                .toList();
        if (FormatValidator.hasValue(existingPolicyIds)) {
            productOptionPricePolicyJpaRepository.deleteByPricePolicyIds(existingPolicyIds);
        }
        pricePolicyJpaRepository.deactivateByProductId(productId);

        // endregion

        List<PricePolicyJpaEntity> savedPricePolicyJpaEntities = pricePolicyJpaRepository.saveAll(newPolicies);
        savedPricePolicyJpaEntities.forEach(PricePolicyJpaEntity::setIdToOrderNum);
        productOptionPricePolicyJpaRepository.saveAll(newOptionPolicies);

        return ProductJpaEntityToDomainMapper.mapToDomainWithoutProductTags(savedCategory).orElse(null);
    }

    private void registerPricePoliciesToOptionCombinations(
            List<ProductOptionCategoryJpaEntity> allCategories,
            ProductOptionCategoryJpaEntity category,
            List<PricePolicyJpaEntity> newPolicies,
            List<ProductOptionPricePolicyJpaEntity> newOptionPolicies,
            ProductJpaEntity product
    ) {
        PricePolicyJpaEntity defaultPricePolicy = product.getDefaultPricePolicy();

        // 상품 가격 정책이 존재하지 않는 경우 예외 발생
        if (FormatValidator.hasNoValue(defaultPricePolicy)) {
            throw new ProductNoPricePolicyException(product.getId());
        }

        // 옵션 카테고리가 새로 생성된 것 하나인 경우 새로 생성된 카테고리의 각 옵션에 대해 가격 정책 생성
        // region

        if (allCategories.size() == 1) {
            category.getProductOptionJpaEntities()
                    .forEach(newOption -> {
                        PricePolicyJpaEntity pricePolicy = createPricePolicyFromDefault(product, product.getDefaultPricePolicy());
                        newPolicies.add(pricePolicy);
                        newOptionPolicies.add(ProductOptionPricePolicyJpaEntity.of(newOption, pricePolicy));
                    });
            return;
        }

        // endregion

        // 기존에 옵션 카테고리가 있는 경우, 모든 카테고리의 각 옵션들을 조합한 결과에 대해 가격 정책 생성(예: 옵션 카테고리가 총 3개인 경우 모든 옵션 3개 조합에 대한 가격 정책 생성)
        // region

        // 옵션 카테고리별로 옵션 목록 정렬
        List<List<ProductOptionJpaEntity>> optionGroups = allCategories.stream()
                .map(ProductOptionCategoryJpaEntity::getProductOptionJpaEntities)
                .toList();

        // 각 옵션 조합 목록 생성
        List<List<ProductOptionJpaEntity>> productOptionCombinations = cartesianProduct(optionGroups);

        productOptionCombinations.forEach(productOptionCombination -> {
            PricePolicyJpaEntity pricePolicy = createPricePolicyFromDefault(product, defaultPricePolicy);
            newPolicies.add(pricePolicy);

            // 옵션 조합과 가격 정책 연결
            productOptionCombination.forEach(
                    option -> newOptionPolicies.add(
                            ProductOptionPricePolicyJpaEntity.of(option, pricePolicy)
                    )
            );
        });

        // endregion
    }

    private List<List<ProductOptionJpaEntity>> cartesianProduct(
            List<List<ProductOptionJpaEntity>> optionGroups
    ) {
        List<List<ProductOptionJpaEntity>> result = new ArrayList<>();
        if (FormatValidator.hasNoValue(optionGroups)) {
            return result;
        }

        backtrackCartesian(optionGroups, 0, new ArrayList<>(), result);
        return result;
    }

    private void backtrackCartesian(
            List<List<ProductOptionJpaEntity>> optionGroups,
            int depth,
            List<ProductOptionJpaEntity> path,
            List<List<ProductOptionJpaEntity>> result
    ) {
        if (depth == optionGroups.size()) {
            result.add(new ArrayList<>(path));
            return;
        }

        for (ProductOptionJpaEntity option : optionGroups.get(depth)) {
            path.add(option);
            backtrackCartesian(optionGroups, depth + 1, path, result);
            path.removeLast();
        }
    }

    private PricePolicyJpaEntity createPricePolicyFromDefault(
            ProductJpaEntity product,
            PricePolicyJpaEntity defaultPricePolicy
    ) {
        PricePolicy pricePolicy = PricePolicy.from(
                PricePolicyCreateState.builder()
                        .price(defaultPricePolicy.getPrice())
                        .discountPrice(defaultPricePolicy.getDiscountPrice())
                        .discountRate(defaultPricePolicy.getDiscountRate())
                        .accumulatedPoint(defaultPricePolicy.getAccumulatedPoint())
                        .accumulationRate(defaultPricePolicy.getAccumulationRate())
                        .build()
        );

        return PricePolicyJpaEntity.from(product, pricePolicy);
    }

    @Override
    public void deleteById(Long id) {
        Long productId = productOptionCategoryJpaRepository.findProductIdByCategoryId(id);
        deleteByIdInternal(productId, id);
    }

    @CacheEvict(value = "product:detail", key = "#productId")
    public void deleteByIdInternal(Long productId, Long id) {
        List<Long> optionIds = productOptionJpaRepository.findIdsByCategoryId(id);
        if (FormatValidator.hasValue(optionIds)) {
            productOptionPricePolicyJpaRepository.deleteByOptionIds(optionIds);
            productOptionJpaRepository.deleteAllByCategoryId(id);
        }

        productOptionCategoryJpaRepository.deleteById(id);
    }

    @Override
    public List<ProductOptionCategory> findActiveWithOptionsByProductId(Long productId) {
        return productOptionCategoryJpaRepository.findActiveWithOptionsByProductId(productId)
                .stream()
                .map(ProductJpaEntityToDomainMapper::mapToDomain)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    @Override
    @CacheEvict(value = "product:detail", key = "#productId")
    public void assignPricePolicyToOptions(Long productId, Long pricePolicyId, List<Long> optionIds) {
        // 기존 동일 옵션 조합 가격 정책 삭제
        List<PricePolicyJpaEntity> existentPricePolicies = pricePolicyJpaRepository.findByOptionIds(optionIds);
        existentPricePolicies.forEach(PricePolicyJpaEntity::deactivate);

        PricePolicyJpaEntity pricePolicyRef = pricePolicyJpaRepository.getReferenceById(pricePolicyId);
        for (Long optionId : optionIds) {
            ProductOptionJpaEntity optionRef = productOptionJpaRepository.getReferenceById(optionId);
            ProductOptionPricePolicyJpaEntity optionPricePolicy = ProductOptionPricePolicyJpaEntity.of(optionRef, pricePolicyRef);
            productOptionPricePolicyJpaRepository.save(optionPricePolicy);
        }
    }
}
