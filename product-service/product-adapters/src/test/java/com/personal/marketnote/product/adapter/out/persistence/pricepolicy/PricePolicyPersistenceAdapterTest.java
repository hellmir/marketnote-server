package com.personal.marketnote.product.adapter.out.persistence.pricepolicy;

import com.personal.marketnote.product.adapter.out.persistence.pricepolicy.entity.PricePolicyJpaEntity;
import com.personal.marketnote.product.adapter.out.persistence.pricepolicy.repository.PricePolicyJpaRepository;
import com.personal.marketnote.product.adapter.out.persistence.product.entity.ProductJpaEntity;
import com.personal.marketnote.product.adapter.out.persistence.product.repository.ProductJpaRepository;
import com.personal.marketnote.product.adapter.out.persistence.productoption.repository.ProductOptionPricePolicyJpaRepository;
import com.personal.marketnote.product.domain.pricepolicy.PricePolicy;
import com.personal.marketnote.product.domain.pricepolicy.PricePolicyCreateState;
import com.personal.marketnote.product.domain.pricepolicy.Rate;
import com.personal.marketnote.product.domain.product.Product;
import com.personal.marketnote.product.domain.product.ProductSearchTarget;
import com.personal.marketnote.product.domain.product.ProductSnapshotState;
import com.personal.marketnote.product.domain.product.ProductSortProperty;
import com.personal.marketnote.product.exception.PricePolicyNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PricePolicyPersistenceAdapterTest {

    @Mock
    private ProductJpaRepository productJpaRepository;

    @Mock
    private PricePolicyJpaRepository pricePolicyJpaRepository;

    @Mock
    private ProductOptionPricePolicyJpaRepository productOptionPricePolicyJpaRepository;

    @InjectMocks
    private PricePolicyPersistenceAdapter adapter;

    private Product product;
    private PricePolicy pricePolicy;

    @BeforeEach
    void setUp() {
        product = Product.from(
                ProductSnapshotState.builder()
                        .id(100L)
                        .name("테스트 상품")
                        .build()
        );
        pricePolicy = PricePolicy.from(
                PricePolicyCreateState.builder()
                        .product(product)
                        .price(10_000L)
                        .discountPrice(8_000L)
                        .discountRate(Rate.of(BigDecimal.valueOf(20.0)))
                        .accumulatedPoint(100L)
                        .accumulationRate(Rate.of(BigDecimal.valueOf(1.0)))
                        .build()
        );
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("가격 정책을 저장하면 저장된 ID를 반환한다")
        void returnsSavedId() {
            ProductJpaEntity productRef = mock(ProductJpaEntity.class);
            PricePolicyJpaEntity savedEntity = mock(PricePolicyJpaEntity.class);

            when(productJpaRepository.getReferenceById(100L)).thenReturn(productRef);
            when(pricePolicyJpaRepository.save(any(PricePolicyJpaEntity.class))).thenReturn(savedEntity);
            when(savedEntity.getId()).thenReturn(555L);

            Long result = adapter.save(pricePolicy);

            assertThat(result).isEqualTo(555L);
            verify(savedEntity).setIdToOrderNum();
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("엔티티가 없으면 empty를 반환한다")
        void returnsEmptyWhenEntityNotFound() {
            when(pricePolicyJpaRepository.findById(1L)).thenReturn(Optional.empty());

            Optional<PricePolicy> result = adapter.findById(1L);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByProductAndOptionIds")
    class FindByProductAndOptionIds {

        @Test
        @DisplayName("옵션 ID가 비어 있으면 empty를 반환한다")
        void returnsEmptyWhenOptionIdsNull() {
            Optional<PricePolicy> result = adapter.findByProductAndOptionIds(100L, null);

            assertThat(result).isEmpty();
            verify(productOptionPricePolicyJpaRepository, never()).findExactMatchingPricePolicyIds(any(), anyLong());
        }

        @Test
        @DisplayName("후보 가격 정책이 없으면 empty를 반환한다")
        void returnsEmptyWhenNoCandidates() {
            List<Long> optionIds = List.of(1L, 2L);
            when(productOptionPricePolicyJpaRepository.findExactMatchingPricePolicyIds(optionIds, 2))
                    .thenReturn(List.of());

            Optional<PricePolicy> result = adapter.findByProductAndOptionIds(100L, optionIds);

            assertThat(result).isEmpty();
            verify(pricePolicyJpaRepository, never()).findAllById(anyList());
        }

        @Test
        @DisplayName("가격 정책의 상품이 요청 상품과 다르면 empty를 반환한다")
        void returnsEmptyWhenProductMismatch() {
            List<Long> optionIds = List.of(1L, 2L);
            ProductJpaEntity productJpaEntity = mock(ProductJpaEntity.class);
            PricePolicyJpaEntity pricePolicyEntity = mock(PricePolicyJpaEntity.class);

            when(productOptionPricePolicyJpaRepository.findExactMatchingPricePolicyIds(optionIds, 2))
                    .thenReturn(List.of(10L));
            when(pricePolicyJpaRepository.findAllById(List.of(10L))).thenReturn(List.of(pricePolicyEntity));
            when(pricePolicyEntity.getProductJpaEntity()).thenReturn(productJpaEntity);
            when(productJpaEntity.getId()).thenReturn(999L);

            Optional<PricePolicy> result = adapter.findByProductAndOptionIds(100L, optionIds);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByOptionIds")
    class FindByOptionIds {

        @Test
        @DisplayName("매칭되는 정책이 없으면 empty를 반환한다")
        void returnsEmptyWhenNotFound() {
            List<Long> optionIds = List.of(1L, 2L);
            when(pricePolicyJpaRepository.findOneByOptionIds(optionIds)).thenReturn(Optional.empty());

            Optional<PricePolicy> result = adapter.findByOptionIds(optionIds);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByProductId")
    class FindByProductId {

        @Test
        @DisplayName("상품의 가격 정책이 없으면 빈 리스트를 반환한다")
        void returnsEmptyListWhenNoEntities() {
            when(pricePolicyJpaRepository.findAllByProductJpaEntity_IdOrderByIdDesc(100L))
                    .thenReturn(List.of());

            List<PricePolicy> result = adapter.findByProductId(100L);

            assertThat(result).isEmpty();
            verify(pricePolicyJpaRepository, never()).findAllWithProductAndOptionMappingsByIdIn(any());
        }
    }

    @Nested
    @DisplayName("findByIds")
    class FindByIds {

        @Test
        @DisplayName("ID 리스트가 비어 있으면 빈 리스트를 반환한다")
        void returnsEmptyListWhenIdsEmpty() {
            List<PricePolicy> result = adapter.findByIds(List.of());

            assertThat(result).isEmpty();
            verify(pricePolicyJpaRepository, never()).findAllWithProductAndOptionMappingsByIdIn(any());
        }

        @Test
        @DisplayName("hydrated 결과가 비어 있으면 빈 리스트를 반환한다")
        void returnsEmptyListWhenHydratedEmpty() {
            when(pricePolicyJpaRepository.findAllWithProductAndOptionMappingsByIdIn(List.of(1L)))
                    .thenReturn(List.of());

            List<PricePolicy> result = adapter.findByIds(List.of(1L));

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("countActivePricePoliciesByCategoryId")
    class CountActive {

        @Test
        @DisplayName("categoryId가 있으면 countActiveByCategoryId를 호출한다")
        void delegatesToByCategory() {
            when(pricePolicyJpaRepository.countActiveByCategoryId(7L, "name", "%k%")).thenReturn(42L);

            long result = adapter.countActivePricePoliciesByCategoryId(7L, ProductSearchTarget.NAME, "k");

            assertThat(result).isEqualTo(42L);
            verify(pricePolicyJpaRepository).countActiveByCategoryId(7L, "name", "%k%");
            verify(pricePolicyJpaRepository, never()).countActive(any(), any());
        }

        @Test
        @DisplayName("categoryId가 null이면 countActive를 호출한다")
        void delegatesToCountActive() {
            when(pricePolicyJpaRepository.countActive("brandName", "")).thenReturn(7L);

            long result = adapter.countActivePricePoliciesByCategoryId(null, ProductSearchTarget.BRAND_NAME, null);

            assertThat(result).isEqualTo(7L);
            verify(pricePolicyJpaRepository).countActive("brandName", "");
            verify(pricePolicyJpaRepository, never()).countActiveByCategoryId(any(), any(), any());
        }
    }

    @Nested
    @DisplayName("findPricePoliciesByOffset")
    class FindPricePoliciesByOffset {

        @Test
        @DisplayName("오름차순이면 findAllActiveByOffsetAsc를 호출한다")
        void delegatesToAsc() {
            when(pricePolicyJpaRepository.findAllActiveByOffsetAsc(
                    any(), any(Pageable.class), any(), any(), any(), any()))
                    .thenReturn(List.of());

            adapter.findPricePoliciesByOffset(
                    List.of(),
                    0,
                    10,
                    Sort.Direction.ASC,
                    ProductSortProperty.ACCUMULATED_POINT,
                    ProductSearchTarget.NAME,
                    null,
                    null
            );

            verify(pricePolicyJpaRepository).findAllActiveByOffsetAsc(
                    eq(List.of()),
                    eq(PageRequest.of(0, 10)),
                    eq("accumulatedPoint"),
                    eq("name"),
                    eq(""),
                    isNull()
            );
        }

        @Test
        @DisplayName("내림차순이면 findAllActiveByOffsetDesc를 호출한다")
        void delegatesToDesc() {
            when(pricePolicyJpaRepository.findAllActiveByOffsetDesc(
                    any(), any(Pageable.class), any(), any(), any(), any()))
                    .thenReturn(List.of());

            adapter.findPricePoliciesByOffset(
                    List.of(),
                    2,
                    10,
                    Sort.Direction.DESC,
                    ProductSortProperty.POPULARITY,
                    ProductSearchTarget.NAME,
                    "k",
                    3L
            );

            verify(pricePolicyJpaRepository).findAllActiveByOffsetDesc(
                    eq(List.of()),
                    eq(PageRequest.of(2, 10)),
                    eq("popularity"),
                    eq("name"),
                    eq("%k%"),
                    eq(3L)
            );
        }
    }

    @Nested
    @DisplayName("deleteById")
    class DeleteById {

        @Test
        @DisplayName("존재하는 가격 정책을 삭제하면 옵션 매핑 삭제 후 비활성화한다")
        void deletesAndDeactivates() {
            PricePolicyJpaEntity entity = mock(PricePolicyJpaEntity.class);
            ProductJpaEntity productEntity = mock(ProductJpaEntity.class);
            when(pricePolicyJpaRepository.findById(10L)).thenReturn(Optional.of(entity));
            when(entity.getProductJpaEntity()).thenReturn(productEntity);
            when(productEntity.getId()).thenReturn(100L);

            adapter.deleteById(10L);

            verify(productOptionPricePolicyJpaRepository).deleteByPricePolicyId(10L);
            verify(pricePolicyJpaRepository, times(2)).findById(10L);
            verify(entity).deactivate();
        }

        @Test
        @DisplayName("존재하지 않는 가격 정책 삭제 시 PricePolicyNotFoundException을 던진다")
        void throwsWhenPricePolicyNotFound() {
            when(pricePolicyJpaRepository.findById(10L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> adapter.deleteById(10L))
                    .isInstanceOf(PricePolicyNotFoundException.class);

            verify(productOptionPricePolicyJpaRepository, never()).deleteByPricePolicyId(anyLong());
        }
    }

    @Nested
    @DisplayName("updateWeeklyPopularity")
    class UpdateWeeklyPopularity {

        @Test
        @DisplayName("전달된 시점을 repository에 위임한다")
        void delegatesToRepository() {
            LocalDateTime since = LocalDateTime.of(2026, 1, 1, 0, 0);

            adapter.updateWeeklyPopularity(since);

            verify(pricePolicyJpaRepository).updateWeeklyPopularity(since);
        }
    }

}
