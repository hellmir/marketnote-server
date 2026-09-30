package com.personal.marketnote.product.adapter.out.persistence.shipping;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.product.adapter.out.persistence.shipping.entity.ShippingPolicyJpaEntity;
import com.personal.marketnote.product.adapter.out.persistence.shipping.repository.ShippingPolicyJpaRepository;
import com.personal.marketnote.product.domain.shipping.ShippingPolicy;
import com.personal.marketnote.product.domain.shipping.ShippingPolicyCreateState;
import com.personal.marketnote.product.exception.ShippingPolicyAlreadyExistsException;
import com.personal.marketnote.product.exception.ShippingPolicyNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShippingPolicyPersistenceAdapterTest {

    @Mock
    private ShippingPolicyJpaRepository shippingPolicyJpaRepository;

    @InjectMocks
    private ShippingPolicyPersistenceAdapter adapter;

    private ShippingPolicy shippingPolicy;

    @BeforeEach
    void setUp() {
        shippingPolicy = ShippingPolicy.from(
                ShippingPolicyCreateState.builder()
                        .sellerId(10L)
                        .deliveryCompany("CJ")
                        .shippingFee(3_000L)
                        .freeShippingThreshold(30_000L)
                        .jejuSurcharge(5_000L)
                        .islandSurcharge(6_000L)
                        .build()
        );
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("배송 정책을 저장하면 저장된 ID를 반환한다")
        void returnsSavedId() {
            ShippingPolicyJpaEntity savedEntity = mock(ShippingPolicyJpaEntity.class);
            when(shippingPolicyJpaRepository.save(any(ShippingPolicyJpaEntity.class))).thenReturn(savedEntity);
            when(savedEntity.getId()).thenReturn(77L);

            Long result = adapter.save(shippingPolicy);

            assertThat(result).isEqualTo(77L);
            ArgumentCaptor<ShippingPolicyJpaEntity> captor = ArgumentCaptor.forClass(ShippingPolicyJpaEntity.class);
            verify(shippingPolicyJpaRepository).save(captor.capture());
            assertThat(captor.getValue().getSellerId()).isEqualTo(10L);
            assertThat(captor.getValue().getDeliveryCompany()).isEqualTo("CJ");
            assertThat(captor.getValue().getShippingFee()).isEqualTo(3_000L);
            assertThat(captor.getValue().getFreeShippingThreshold()).isEqualTo(30_000L);
            assertThat(captor.getValue().getJejuSurcharge()).isEqualTo(5_000L);
            assertThat(captor.getValue().getIslandSurcharge()).isEqualTo(6_000L);
        }

        @Test
        @DisplayName("DataIntegrityViolationException이 발생하면 ShippingPolicyAlreadyExistsException을 던진다")
        void throwsAlreadyExistsOnUniqueViolation() {
            when(shippingPolicyJpaRepository.save(any(ShippingPolicyJpaEntity.class)))
                    .thenThrow(new DataIntegrityViolationException("unique constraint"));

            assertThatThrownBy(() -> adapter.save(shippingPolicy))
                    .isInstanceOf(ShippingPolicyAlreadyExistsException.class);
        }
    }

    @Nested
    @DisplayName("findActiveBySellerId")
    class FindActiveBySellerId {

        @Test
        @DisplayName("활성 배송 정책이 없으면 empty를 반환한다")
        void returnsEmpty() {
            when(shippingPolicyJpaRepository.findBySellerIdAndStatus(10L, EntityStatus.ACTIVE))
                    .thenReturn(Optional.empty());

            Optional<ShippingPolicy> result = adapter.findActiveBySellerId(10L);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findActiveBySellerIds")
    class FindActiveBySellerIds {

        @Test
        @DisplayName("활성 배송 정책이 없으면 빈 리스트를 반환한다")
        void returnsEmptyList() {
            when(shippingPolicyJpaRepository.findAllBySellerIdInAndStatus(List.of(10L, 20L), EntityStatus.ACTIVE))
                    .thenReturn(List.of());

            List<ShippingPolicy> result = adapter.findActiveBySellerIds(List.of(10L, 20L));

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("기존 엔티티가 있으면 엔티티 상태를 업데이트한다")
        void updatesExistingEntity() {
            ShippingPolicy updatedPolicy = ShippingPolicy.from(
                    ShippingPolicyCreateState.builder()
                            .sellerId(10L)
                            .deliveryCompany("CJ")
                            .shippingFee(4_000L)
                            .freeShippingThreshold(50_000L)
                            .jejuSurcharge(6_000L)
                            .islandSurcharge(7_000L)
                            .build()
            );
            ShippingPolicyJpaEntity entity = mock(ShippingPolicyJpaEntity.class);
            when(shippingPolicyJpaRepository.findById(updatedPolicy.getId())).thenReturn(Optional.of(entity));

            adapter.update(updatedPolicy);

            verify(entity).updateFrom(updatedPolicy);
        }

        @Test
        @DisplayName("기존 엔티티가 없으면 ShippingPolicyNotFoundException을 던진다")
        void throwsWhenNotFound() {
            when(shippingPolicyJpaRepository.findById(any())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> adapter.update(shippingPolicy))
                    .isInstanceOf(ShippingPolicyNotFoundException.class);
            verify(shippingPolicyJpaRepository, never()).save(any(ShippingPolicyJpaEntity.class));
        }
    }
}
