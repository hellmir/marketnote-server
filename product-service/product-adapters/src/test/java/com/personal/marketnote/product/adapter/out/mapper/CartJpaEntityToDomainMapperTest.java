package com.personal.marketnote.product.adapter.out.mapper;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.product.adapter.out.persistence.cart.entity.CartId;
import com.personal.marketnote.product.adapter.out.persistence.cart.entity.CartProductJpaEntity;
import com.personal.marketnote.product.adapter.out.persistence.pricepolicy.entity.PricePolicyJpaEntity;
import com.personal.marketnote.product.domain.cart.CartProduct;
import com.personal.marketnote.product.domain.pricepolicy.PricePolicy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CartJpaEntityToDomainMapperTest {

    @Nested
    @DisplayName("mapToDomain")
    class MapToDomain {

        @Test
        @DisplayName("엔티티가 null이면 empty를 반환한다")
        void returnsEmptyWhenNull() {
            Optional<CartProduct> result = CartJpaEntityToDomainMapper.mapToDomain(null);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("엔티티가 있으면 userId/sharerKey/quantity/imageUrl/status를 도메인으로 매핑한다")
        void mapsEntityFields() {
            UUID sharerKey = UUID.randomUUID();
            CartProductJpaEntity entity = mock(CartProductJpaEntity.class);
            PricePolicyJpaEntity policyEntity = mock(PricePolicyJpaEntity.class);
            when(entity.getId()).thenReturn(new CartId(10L, 500L));
            when(entity.getSharerKey()).thenReturn(sharerKey);
            when(entity.getImageUrl()).thenReturn("https://cdn.example.com/img.png");
            when(entity.getQuantity()).thenReturn((short) 3);
            when(entity.getStatus()).thenReturn(EntityStatus.ACTIVE);
            when(entity.getPricePolicyJpaEntity()).thenReturn(policyEntity);

            try (MockedStatic<PricePolicyJpaEntityToDomainMapper> mapperMock
                         = Mockito.mockStatic(PricePolicyJpaEntityToDomainMapper.class)) {
                mapperMock.when(() -> PricePolicyJpaEntityToDomainMapper.mapToDomainWithOptions(policyEntity))
                        .thenReturn(Optional.empty());

                Optional<CartProduct> result = CartJpaEntityToDomainMapper.mapToDomain(entity);

                assertThat(result).isPresent();
                assertThat(result.get().getUserId()).isEqualTo(10L);
                assertThat(result.get().getSharerKey()).isEqualTo(sharerKey);
                assertThat(result.get().getImageUrl()).isEqualTo("https://cdn.example.com/img.png");
                assertThat(result.get().getQuantity().getValue()).isEqualTo(3);
                assertThat(result.get().getStatus()).isEqualTo(EntityStatus.ACTIVE);
            }
        }
    }
}
