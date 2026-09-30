package com.personal.marketnote.product.adapter.out.mapper;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.product.adapter.out.persistence.pricepolicy.entity.PricePolicyJpaEntity;
import com.personal.marketnote.product.adapter.out.persistence.product.entity.ProductJpaEntity;
import com.personal.marketnote.product.adapter.out.persistence.productoption.entity.ProductOptionPricePolicyJpaEntity;
import com.personal.marketnote.product.domain.pricepolicy.PricePolicy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PricePolicyJpaEntityToDomainMapperTest {

    @Nested
    @DisplayName("mapToDomain(entity)")
    class MapToDomain {

        @Test
        @DisplayName("엔티티가 null이면 empty를 반환한다")
        void returnsEmptyWhenNull() {
            Optional<PricePolicy> result = PricePolicyJpaEntityToDomainMapper.mapToDomain(null);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("엔티티가 있으면 필드들을 도메인 객체로 매핑한다")
        void mapsEntityFields() {
            PricePolicyJpaEntity entity = mockEntity();
            when(entity.getProductJpaEntity()).thenReturn(null);

            Optional<PricePolicy> result = PricePolicyJpaEntityToDomainMapper.mapToDomain(entity);

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(1L);
            assertThat(result.get().getPrice().getValue()).isEqualTo(10_000L);
            assertThat(result.get().getDiscountPrice().getValue()).isEqualTo(8_000L);
            assertThat(result.get().getDiscountRate().getValue()).isEqualByComparingTo(BigDecimal.valueOf(20.0));
            assertThat(result.get().getAccumulatedPoint().getValue()).isEqualTo(100L);
            assertThat(result.get().getAccumulationRate().getValue()).isEqualByComparingTo(BigDecimal.valueOf(1.0));
            assertThat(result.get().getPopularity()).isEqualTo(50L);
            assertThat(result.get().getStatus()).isEqualTo(EntityStatus.ACTIVE);
            assertThat(result.get().getOrderNum()).isEqualTo(7L);
        }
    }

    @Nested
    @DisplayName("mapToDomain(entity, optionIds)")
    class MapToDomainWithOptionIds {

        @Test
        @DisplayName("엔티티가 null이면 empty를 반환한다")
        void returnsEmptyWhenNull() {
            Optional<PricePolicy> result = PricePolicyJpaEntityToDomainMapper.mapToDomain(null, List.of(1L));

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("엔티티가 있으면 optionIds도 함께 매핑한다")
        void mapsWithOptionIds() {
            PricePolicyJpaEntity entity = mockEntity();
            when(entity.getProductJpaEntity()).thenReturn(null);

            Optional<PricePolicy> result = PricePolicyJpaEntityToDomainMapper.mapToDomain(entity, List.of(10L, 20L));

            assertThat(result).isPresent();
            assertThat(result.get().getOptionIds()).containsExactly(10L, 20L);
        }
    }

    @Nested
    @DisplayName("mapToDomainWithOptions")
    class MapToDomainWithOptions {

        @Test
        @DisplayName("연관된 옵션이 없으면 빈 productOptions로 매핑한다")
        void mapsWithEmptyProductOptions() {
            PricePolicyJpaEntity entity = mockEntity();
            when(entity.getProductJpaEntity()).thenReturn(null);
            when(entity.getProductOptionPricePolicyJpaEntities()).thenReturn(List.of());

            Optional<PricePolicy> result = PricePolicyJpaEntityToDomainMapper.mapToDomainWithOptions(entity);

            assertThat(result).isPresent();
            assertThat(result.get().getProductOptions()).isEmpty();
        }
    }

    private PricePolicyJpaEntity mockEntity() {
        PricePolicyJpaEntity entity = mock(PricePolicyJpaEntity.class);
        when(entity.getId()).thenReturn(1L);
        when(entity.getPrice()).thenReturn(10_000L);
        when(entity.getDiscountPrice()).thenReturn(8_000L);
        when(entity.getDiscountRate()).thenReturn(BigDecimal.valueOf(20.0));
        when(entity.getAccumulatedPoint()).thenReturn(100L);
        when(entity.getAccumulationRate()).thenReturn(BigDecimal.valueOf(1.0));
        when(entity.getPopularity()).thenReturn(50L);
        when(entity.getStatus()).thenReturn(EntityStatus.ACTIVE);
        when(entity.getOrderNum()).thenReturn(7L);
        return entity;
    }
}
