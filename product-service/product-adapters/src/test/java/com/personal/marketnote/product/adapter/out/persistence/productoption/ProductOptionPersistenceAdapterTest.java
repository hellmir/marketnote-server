package com.personal.marketnote.product.adapter.out.persistence.productoption;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.product.adapter.out.persistence.pricepolicy.entity.PricePolicyJpaEntity;
import com.personal.marketnote.product.adapter.out.persistence.pricepolicy.repository.PricePolicyJpaRepository;
import com.personal.marketnote.product.adapter.out.persistence.product.entity.ProductJpaEntity;
import com.personal.marketnote.product.adapter.out.persistence.product.repository.ProductJpaRepository;
import com.personal.marketnote.product.adapter.out.persistence.productoption.entity.ProductOptionCategoryJpaEntity;
import com.personal.marketnote.product.adapter.out.persistence.productoption.entity.ProductOptionPricePolicyJpaEntity;
import com.personal.marketnote.product.adapter.out.persistence.productoption.repository.ProductOptionCategoryJpaRepository;
import com.personal.marketnote.product.adapter.out.persistence.productoption.repository.ProductOptionJpaRepository;
import com.personal.marketnote.product.adapter.out.persistence.productoption.repository.ProductOptionPricePolicyJpaRepository;
import com.personal.marketnote.product.exception.InvalidProductOptionOwnershipException;
import com.personal.marketnote.product.domain.option.ProductOptionCategory;
import com.personal.marketnote.product.domain.option.ProductOptionCategoryCreateState;
import com.personal.marketnote.product.domain.option.ProductOptionCreateState;
import com.personal.marketnote.product.domain.pricepolicy.PricePolicy;
import com.personal.marketnote.product.domain.pricepolicy.Rate;
import com.personal.marketnote.product.domain.pricepolicy.PricePolicyCreateState;
import com.personal.marketnote.product.domain.product.Product;
import com.personal.marketnote.product.domain.product.ProductSnapshotState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductOptionPersistenceAdapterTest {
    @Mock
    private ProductJpaRepository productJpaRepository;
    @Mock
    private ProductOptionCategoryJpaRepository productOptionCategoryJpaRepository;
    @Mock
    private ProductOptionJpaRepository productOptionJpaRepository;
    @Mock
    private ProductOptionPricePolicyJpaRepository productOptionPricePolicyJpaRepository;
    @Mock
    private PricePolicyJpaRepository pricePolicyJpaRepository;

    @InjectMocks
    private ProductOptionPersistenceAdapter adapter;

    @SuppressWarnings("unchecked")
    @Test
    void save_whenTwoExistingCategoriesAndNewOne_createsOnlyFullCombinationsAndDeactivatesOldPolicies() {
        long productId = 1L;
        Product product = buildProduct(productId);
        ProductOptionCategory newCategory = buildCategory(product, "C", 4);

        ProductJpaEntity productJpaEntity = org.mockito.Mockito.mock(ProductJpaEntity.class);
        PricePolicyJpaEntity defaultPolicy = buildDefaultPolicy(productJpaEntity);
        when(productJpaEntity.getDefaultPricePolicy()).thenReturn(defaultPolicy);
        when(productJpaRepository.findById(productId)).thenReturn(Optional.of(productJpaEntity));

        ProductOptionCategoryJpaEntity savedCategory = ProductOptionCategoryJpaEntity.from(newCategory, productJpaEntity);
        ProductOptionCategoryJpaEntity existingA = buildCategoryEntity(productJpaEntity, product, "A", 2);
        ProductOptionCategoryJpaEntity existingB = buildCategoryEntity(productJpaEntity, product, "B", 3);

        when(productOptionCategoryJpaRepository.save(any(ProductOptionCategoryJpaEntity.class))).thenReturn(savedCategory);
        when(productOptionCategoryJpaRepository.findActiveWithOptionsByProductId(productId))
                .thenReturn(List.of(existingA, existingB, savedCategory));
        when(pricePolicyJpaRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(productOptionPricePolicyJpaRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        adapter.save(newCategory);

        verify(pricePolicyJpaRepository).deactivateByProductId(productId);

        ArgumentCaptor<List<PricePolicyJpaEntity>> policiesCaptor = ArgumentCaptor.forClass(List.class);
        verify(pricePolicyJpaRepository).saveAll(policiesCaptor.capture());
        int expectedPolicies = 2 * 3 * 4;
        assertThat(policiesCaptor.getValue()).hasSize(expectedPolicies);

        ArgumentCaptor<List<ProductOptionPricePolicyJpaEntity>> mappingsCaptor = ArgumentCaptor.forClass(List.class);
        verify(productOptionPricePolicyJpaRepository).saveAll(mappingsCaptor.capture());
        List<ProductOptionPricePolicyJpaEntity> mappings = mappingsCaptor.getValue();
        assertThat(mappings).hasSize(expectedPolicies * 3);

        Map<PricePolicyJpaEntity, Long> counts = mappings.stream()
                .collect(Collectors.groupingBy(
                        ProductOptionPricePolicyJpaEntity::getPricePolicyJpaEntity,
                        Collectors.counting()
                ));
        assertThat(counts).hasSize(expectedPolicies);
        assertThat(counts.values()).allMatch(count -> count == 3L);
    }

    @SuppressWarnings("unchecked")
    @Test
    void save_whenOnlyOneCategory_createsSingleOptionPoliciesAndDeactivatesOldPolicies() {
        long productId = 2L;
        Product product = buildProduct(productId);
        ProductOptionCategory newCategory = buildCategory(product, "A", 3);

        ProductJpaEntity productJpaEntity = org.mockito.Mockito.mock(ProductJpaEntity.class);
        PricePolicyJpaEntity defaultPolicy = buildDefaultPolicy(productJpaEntity);
        when(productJpaEntity.getDefaultPricePolicy()).thenReturn(defaultPolicy);
        when(productJpaRepository.findById(productId)).thenReturn(Optional.of(productJpaEntity));

        ProductOptionCategoryJpaEntity savedCategory = ProductOptionCategoryJpaEntity.from(newCategory, productJpaEntity);
        when(productOptionCategoryJpaRepository.save(any(ProductOptionCategoryJpaEntity.class))).thenReturn(savedCategory);
        when(productOptionCategoryJpaRepository.findActiveWithOptionsByProductId(productId))
                .thenReturn(List.of(savedCategory));
        when(pricePolicyJpaRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(productOptionPricePolicyJpaRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        adapter.save(newCategory);

        verify(pricePolicyJpaRepository).deactivateByProductId(productId);

        ArgumentCaptor<List<PricePolicyJpaEntity>> policiesCaptor = ArgumentCaptor.forClass(List.class);
        verify(pricePolicyJpaRepository).saveAll(policiesCaptor.capture());
        int expectedPolicies = 3;
        assertThat(policiesCaptor.getValue()).hasSize(expectedPolicies);

        ArgumentCaptor<List<ProductOptionPricePolicyJpaEntity>> mappingsCaptor = ArgumentCaptor.forClass(List.class);
        verify(productOptionPricePolicyJpaRepository).saveAll(mappingsCaptor.capture());
        List<ProductOptionPricePolicyJpaEntity> mappings = mappingsCaptor.getValue();
        assertThat(mappings).hasSize(expectedPolicies);

        Map<PricePolicyJpaEntity, Long> counts = mappings.stream()
                .collect(Collectors.groupingBy(
                        ProductOptionPricePolicyJpaEntity::getPricePolicyJpaEntity,
                        Collectors.counting()
                ));
        assertThat(counts).hasSize(expectedPolicies);
        assertThat(counts.values()).allMatch(count -> count == 1L);
    }

    @Test
    @DisplayName("assignPricePolicyToOptions: 요청한 옵션이 모두 상품 소유면 매핑을 저장한다")
    void assignPricePolicyToOptions_allOwned_savesMappings() {
        long productId = 10L;
        long pricePolicyId = 100L;
        List<Long> optionIds = List.of(201L, 202L);

        when(productOptionJpaRepository.countByProductIdAndIds(productId, optionIds)).thenReturn(2L);
        when(pricePolicyJpaRepository.findByOptionIds(optionIds)).thenReturn(List.of());
        when(pricePolicyJpaRepository.getReferenceById(pricePolicyId))
                .thenReturn(org.mockito.Mockito.mock(PricePolicyJpaEntity.class));
        when(productOptionJpaRepository.getReferenceById(any()))
                .thenAnswer(invocation -> {
                    Long optionId = invocation.getArgument(0);
                    return buildOptionRefMock(optionId);
                });

        adapter.assignPricePolicyToOptions(productId, pricePolicyId, optionIds);

        verify(productOptionJpaRepository).countByProductIdAndIds(productId, optionIds);
        verify(productOptionPricePolicyJpaRepository, org.mockito.Mockito.times(2))
                .save(any(ProductOptionPricePolicyJpaEntity.class));
    }

    @Test
    @DisplayName("assignPricePolicyToOptions: 요청한 옵션 중 타 상품 소유가 포함되면 예외를 던지고 저장하지 않는다")
    void assignPricePolicyToOptions_foreignOption_throwsAndSkipsSave() {
        long productId = 11L;
        long pricePolicyId = 110L;
        List<Long> optionIds = List.of(301L, 302L);

        when(productOptionJpaRepository.countByProductIdAndIds(productId, optionIds)).thenReturn(1L);

        assertThatThrownBy(() -> adapter.assignPricePolicyToOptions(productId, pricePolicyId, optionIds))
                .isInstanceOf(InvalidProductOptionOwnershipException.class);

        verify(productOptionPricePolicyJpaRepository, never()).save(any());
        verifyNoInteractions(pricePolicyJpaRepository);
    }

    private com.personal.marketnote.product.adapter.out.persistence.productoption.entity.ProductOptionJpaEntity buildOptionRefMock(Long optionId) {
        com.personal.marketnote.product.adapter.out.persistence.productoption.entity.ProductOptionJpaEntity mock =
                org.mockito.Mockito.mock(com.personal.marketnote.product.adapter.out.persistence.productoption.entity.ProductOptionJpaEntity.class);
        when(mock.getId()).thenReturn(optionId);
        return mock;
    }

    private Product buildProduct(long id) {
        return Product.from(
                ProductSnapshotState.builder()
                        .id(id)
                        .status(EntityStatus.ACTIVE)
                        .build()
        );
    }

    private ProductOptionCategory buildCategory(Product product, String name, int optionCount) {
        List<ProductOptionCreateState> optionStates = IntStream.range(0, optionCount)
                .mapToObj(index -> ProductOptionCreateState.builder()
                        .category(null)
                        .content(name + "-" + index)
                        .build())
                .toList();

        return ProductOptionCategory.from(
                ProductOptionCategoryCreateState.builder()
                        .product(product)
                        .name(name)
                        .optionStates(optionStates)
                        .build()
        );
    }

    private ProductOptionCategoryJpaEntity buildCategoryEntity(
            ProductJpaEntity productJpaEntity,
            Product product,
            String name,
            int optionCount
    ) {
        return ProductOptionCategoryJpaEntity.from(
                buildCategory(product, name, optionCount),
                productJpaEntity
        );
    }

    private PricePolicyJpaEntity buildDefaultPolicy(ProductJpaEntity productJpaEntity) {
        PricePolicy pricePolicy = PricePolicy.from(
                PricePolicyCreateState.builder()
                        .price(100L)
                        .discountPrice(80L)
                        .discountRate(Rate.of(BigDecimal.valueOf(20.0)))
                        .accumulatedPoint(10L)
                        .accumulationRate(Rate.of(BigDecimal.valueOf(1.0)))
                        .build()
        );

        return PricePolicyJpaEntity.from(productJpaEntity, pricePolicy);
    }
}
