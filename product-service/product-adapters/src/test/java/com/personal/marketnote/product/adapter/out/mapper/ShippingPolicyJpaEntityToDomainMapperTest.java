package com.personal.marketnote.product.adapter.out.mapper;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.product.adapter.out.persistence.shipping.entity.ShippingPolicyJpaEntity;
import com.personal.marketnote.product.domain.shipping.ShippingPolicy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShippingPolicyJpaEntityToDomainMapperTest {

    @Nested
    @DisplayName("mapToDomain(entity)")
    class MapToDomain {

        @Test
        @DisplayName("엔티티가 null이면 empty를 반환한다")
        void returnsEmptyWhenNull() {
            Optional<ShippingPolicy> result = ShippingPolicyJpaEntityToDomainMapper.mapToDomain(null);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("엔티티의 모든 필드를 도메인 객체로 매핑한다")
        void mapsAllFieldsToDomain() {
            LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 0, 0);
            LocalDateTime modifiedAt = LocalDateTime.of(2026, 2, 1, 0, 0);
            ShippingPolicyJpaEntity entity = mock(ShippingPolicyJpaEntity.class);
            when(entity.getId()).thenReturn(7L);
            when(entity.getSellerId()).thenReturn(123L);
            when(entity.getDeliveryCompany()).thenReturn("CJ");
            when(entity.getShippingFee()).thenReturn(3_000L);
            when(entity.getFreeShippingThreshold()).thenReturn(50_000L);
            when(entity.getJejuSurcharge()).thenReturn(5_000L);
            when(entity.getIslandSurcharge()).thenReturn(6_000L);
            when(entity.getStatus()).thenReturn(EntityStatus.ACTIVE);
            when(entity.getCreatedAt()).thenReturn(createdAt);
            when(entity.getModifiedAt()).thenReturn(modifiedAt);

            Optional<ShippingPolicy> result = ShippingPolicyJpaEntityToDomainMapper.mapToDomain(entity);

            assertThat(result).isPresent();
            ShippingPolicy policy = result.get();
            assertThat(policy.getId()).isEqualTo(7L);
            assertThat(policy.getSellerId()).isEqualTo(123L);
            assertThat(policy.getDeliveryCompany()).isEqualTo("CJ");
            assertThat(policy.getShippingFee().getValue()).isEqualTo(3_000L);
            assertThat(policy.getFreeShippingThreshold().getValue()).isEqualTo(50_000L);
            assertThat(policy.getJejuSurcharge().getValue()).isEqualTo(5_000L);
            assertThat(policy.getIslandSurcharge().getValue()).isEqualTo(6_000L);
            assertThat(policy.getStatus()).isEqualTo(EntityStatus.ACTIVE);
            assertThat(policy.getCreatedAt()).isEqualTo(createdAt);
            assertThat(policy.getModifiedAt()).isEqualTo(modifiedAt);
        }

        @Test
        @DisplayName("제주/도서산간 추가 배송비가 null이면 0원으로 매핑한다")
        void resolvesNullSurchargesToZero() {
            ShippingPolicyJpaEntity entity = mock(ShippingPolicyJpaEntity.class);
            when(entity.getId()).thenReturn(8L);
            when(entity.getSellerId()).thenReturn(124L);
            when(entity.getDeliveryCompany()).thenReturn("한진");
            when(entity.getShippingFee()).thenReturn(2_500L);
            when(entity.getFreeShippingThreshold()).thenReturn(40_000L);
            when(entity.getJejuSurcharge()).thenReturn(null);
            when(entity.getIslandSurcharge()).thenReturn(null);
            when(entity.getStatus()).thenReturn(EntityStatus.ACTIVE);

            Optional<ShippingPolicy> result = ShippingPolicyJpaEntityToDomainMapper.mapToDomain(entity);

            assertThat(result).isPresent();
            assertThat(result.get().getJejuSurcharge().getValue()).isEqualTo(0L);
            assertThat(result.get().getIslandSurcharge().getValue()).isEqualTo(0L);
        }

        @Test
        @DisplayName("INACTIVE 상태가 도메인 객체로 그대로 전달된다")
        void preservesInactiveStatus() {
            ShippingPolicyJpaEntity entity = mock(ShippingPolicyJpaEntity.class);
            when(entity.getId()).thenReturn(9L);
            when(entity.getSellerId()).thenReturn(125L);
            when(entity.getDeliveryCompany()).thenReturn("우체국");
            when(entity.getShippingFee()).thenReturn(2_000L);
            when(entity.getFreeShippingThreshold()).thenReturn(30_000L);
            when(entity.getJejuSurcharge()).thenReturn(0L);
            when(entity.getIslandSurcharge()).thenReturn(0L);
            when(entity.getStatus()).thenReturn(EntityStatus.INACTIVE);

            Optional<ShippingPolicy> result = ShippingPolicyJpaEntityToDomainMapper.mapToDomain(entity);

            assertThat(result).isPresent();
            assertThat(result.get().getStatus()).isEqualTo(EntityStatus.INACTIVE);
        }
    }
}
