package com.personal.marketnote.product.adapter.out.persistence.cart;

import com.personal.marketnote.product.adapter.out.mapper.CartJpaEntityToDomainMapper;
import com.personal.marketnote.product.adapter.out.persistence.cart.entity.CartProductJpaEntity;
import com.personal.marketnote.product.adapter.out.persistence.cart.repository.CartJpaRepository;
import com.personal.marketnote.product.adapter.out.persistence.pricepolicy.entity.PricePolicyJpaEntity;
import com.personal.marketnote.product.adapter.out.persistence.pricepolicy.repository.PricePolicyJpaRepository;
import com.personal.marketnote.product.domain.cart.CartProduct;
import com.personal.marketnote.product.domain.cart.CartProductCreateState;
import com.personal.marketnote.product.domain.pricepolicy.PricePolicy;
import com.personal.marketnote.product.domain.pricepolicy.PricePolicySnapshotState;
import com.personal.marketnote.product.domain.pricepolicy.Rate;
import com.personal.marketnote.product.exception.CartProductNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartPersistenceAdapterTest {

    @Mock
    private CartJpaRepository cartJpaRepository;

    @Mock
    private PricePolicyJpaRepository pricePolicyJpaRepository;

    @InjectMocks
    private CartPersistenceAdapter adapter;

    private CartProduct cartProduct;
    private PricePolicy pricePolicy;

    @BeforeEach
    void setUp() {
        pricePolicy = PricePolicy.from(
                PricePolicySnapshotState.builder()
                        .id(500L)
                        .price(10_000L)
                        .discountPrice(8_000L)
                        .discountRate(Rate.of(BigDecimal.valueOf(20.0)))
                        .accumulatedPoint(100L)
                        .accumulationRate(Rate.of(BigDecimal.valueOf(1.0)))
                        .build()
        );
        cartProduct = CartProduct.from(
                CartProductCreateState.builder()
                        .userId(10L)
                        .sharerKey(UUID.randomUUID())
                        .pricePolicy(pricePolicy)
                        .imageUrl("https://cdn.example.com/img.png")
                        .quantity((short) 2)
                        .build()
        );
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("장바구니 상품을 저장하고 repository에 위임한다")
        void savesEntity() {
            PricePolicyJpaEntity pricePolicyRef = mock(PricePolicyJpaEntity.class);
            CartProductJpaEntity savedEntity = mock(CartProductJpaEntity.class);
            when(pricePolicyJpaRepository.getReferenceById(500L)).thenReturn(pricePolicyRef);
            when(pricePolicyRef.getId()).thenReturn(500L);
            when(cartJpaRepository.save(any(CartProductJpaEntity.class))).thenReturn(savedEntity);

            try (MockedStatic<CartJpaEntityToDomainMapper> mapperMock
                         = Mockito.mockStatic(CartJpaEntityToDomainMapper.class)) {
                mapperMock.when(() -> CartJpaEntityToDomainMapper.mapToDomain(savedEntity))
                        .thenReturn(Optional.of(cartProduct));

                CartProduct result = adapter.save(cartProduct);

                assertThat(result).isSameAs(cartProduct);
            }

            ArgumentCaptor<CartProductJpaEntity> captor = ArgumentCaptor.forClass(CartProductJpaEntity.class);
            verify(cartJpaRepository).save(captor.capture());
            assertThat(captor.getValue().getId().getUserId()).isEqualTo(10L);
            assertThat(captor.getValue().getId().getPricePolicyId()).isEqualTo(500L);
            assertThat(captor.getValue().getQuantity()).isEqualTo((short) 2);
        }
    }

    @Nested
    @DisplayName("findByUserId")
    class FindByUserId {

        @Test
        @DisplayName("조회 결과가 없으면 빈 리스트를 반환한다")
        void returnsEmptyList() {
            when(cartJpaRepository.findByIdUserId(10L)).thenReturn(List.of());

            List<CartProduct> result = adapter.findByUserId(10L);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findCartProductByUserIdAndPricePolicyId")
    class FindOne {

        @Test
        @DisplayName("엔티티가 없으면 empty를 반환한다")
        void returnsEmpty() {
            when(cartJpaRepository.findByIdUserIdAndPricePolicyId(10L, 500L))
                    .thenReturn(Optional.empty());

            Optional<CartProduct> result = adapter.findCartProductByUserIdAndPricePolicyId(10L, 500L);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("existsByUserIdAndPolicyId")
    class Exists {

        @Test
        @DisplayName("repository에 위임한다")
        void delegatesToRepository() {
            when(cartJpaRepository.existsByUserIdAndPolicyId(10L, 500L)).thenReturn(true);

            boolean result = adapter.existsByUserIdAndPolicyId(10L, 500L);

            assertThat(result).isTrue();
            verify(cartJpaRepository).existsByUserIdAndPolicyId(10L, 500L);
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("가격 정책이 바뀌지 않으면 기존 엔티티 상태만 변경한다")
        void updatesEntityInPlaceWhenPolicyUnchanged() {
            CartProductJpaEntity existingEntity = mock(CartProductJpaEntity.class);
            when(cartJpaRepository.findByIdUserIdAndPricePolicyId(10L, 500L))
                    .thenReturn(Optional.of(existingEntity));

            adapter.update(cartProduct, 500L);

            verify(existingEntity).updateFrom(cartProduct);
            verify(cartJpaRepository, never()).delete(any(CartProductJpaEntity.class));
            verify(cartJpaRepository, never()).save(any(CartProductJpaEntity.class));
        }

        @Test
        @DisplayName("가격 정책이 바뀌면 기존 엔티티 삭제 후 새 엔티티를 저장한다")
        void deletesAndInsertsWhenPolicyChanged() {
            PricePolicy newPricePolicy = PricePolicy.from(
                    PricePolicySnapshotState.builder()
                            .id(600L)
                            .price(5_000L)
                            .discountPrice(4_000L)
                            .discountRate(Rate.of(BigDecimal.valueOf(20.0)))
                            .accumulatedPoint(50L)
                            .accumulationRate(Rate.of(BigDecimal.valueOf(1.0)))
                            .build()
            );
            CartProduct changedCartProduct = CartProduct.from(
                    CartProductCreateState.builder()
                            .userId(10L)
                            .sharerKey(UUID.randomUUID())
                            .pricePolicy(newPricePolicy)
                            .imageUrl("https://cdn.example.com/new.png")
                            .quantity((short) 1)
                            .build()
            );

            CartProductJpaEntity existingEntity = mock(CartProductJpaEntity.class);
            PricePolicyJpaEntity newPricePolicyRef = mock(PricePolicyJpaEntity.class);
            when(cartJpaRepository.findByIdUserIdAndPricePolicyId(10L, 500L))
                    .thenReturn(Optional.of(existingEntity));
            when(pricePolicyJpaRepository.getReferenceById(600L)).thenReturn(newPricePolicyRef);
            when(newPricePolicyRef.getId()).thenReturn(600L);

            adapter.update(changedCartProduct, 500L);

            verify(cartJpaRepository).delete(existingEntity);
            verify(existingEntity, never()).updateFrom(any(CartProduct.class));
            ArgumentCaptor<CartProductJpaEntity> captor = ArgumentCaptor.forClass(CartProductJpaEntity.class);
            verify(cartJpaRepository).save(captor.capture());
            assertThat(captor.getValue().getId().getPricePolicyId()).isEqualTo(600L);
        }

        @Test
        @DisplayName("기존 엔티티가 없으면 CartProductNotFoundException을 던진다")
        void throwsWhenNotFound() {
            when(cartJpaRepository.findByIdUserIdAndPricePolicyId(10L, 500L))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> adapter.update(cartProduct, 500L))
                    .isInstanceOf(CartProductNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("지정한 userId/pricePolicyIds의 엔티티들을 삭제한다")
        void deletesByIds() {
            adapter.delete(10L, List.of(500L, 600L));

            verify(cartJpaRepository).deleteByUserIdAndPricePolicyIdIn(10L, List.of(500L, 600L));
        }
    }

    @Nested
    @DisplayName("deleteAll")
    class DeleteAll {

        @Test
        @DisplayName("지정한 userId의 모든 엔티티를 삭제한다")
        void deletesAll() {
            adapter.deleteAll(10L);

            verify(cartJpaRepository).deleteByUserId(10L);
        }
    }
}
