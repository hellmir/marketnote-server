package com.personal.marketnote.product.adapter.out.mapper;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.product.adapter.out.persistence.product.entity.ProductJpaEntity;
import com.personal.marketnote.product.adapter.out.persistence.productoption.entity.ProductOptionCategoryJpaEntity;
import com.personal.marketnote.product.adapter.out.persistence.productoption.entity.ProductOptionJpaEntity;
import com.personal.marketnote.product.domain.option.ProductOption;
import com.personal.marketnote.product.domain.option.ProductOptionCategory;
import com.personal.marketnote.product.domain.product.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProductJpaEntityToDomainMapperTest {

    @Nested
    @DisplayName("mapToDomain(ProductJpaEntity)")
    class MapToDomain {

        @Test
        @DisplayName("엔티티가 null이면 empty를 반환한다")
        void returnsEmptyWhenNull() {
            Optional<Product> result = ProductJpaEntityToDomainMapper.mapToDomain((ProductJpaEntity) null);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("엔티티가 있으면 필드를 도메인으로 매핑한다")
        void mapsEntityFields() {
            UUID productKey = UUID.randomUUID();
            ProductJpaEntity entity = productMock(productKey);

            Optional<Product> result = ProductJpaEntityToDomainMapper.mapToDomain(entity);

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(1L);
            assertThat(result.get().getProductKey()).isEqualTo(productKey);
            assertThat(result.get().getSellerId()).isEqualTo(10L);
            assertThat(result.get().getName()).isEqualTo("상품명");
            assertThat(result.get().getBrandName()).isEqualTo("브랜드");
            assertThat(result.get().getDetail()).isEqualTo("상세");
            assertThat(result.get().getSales()).isEqualTo(5);
            assertThat(result.get().getViewCount()).isEqualTo(100L);
            assertThat(result.get().getPopularity()).isEqualTo(50L);
            assertThat(result.get().isFindAllOptionsYn()).isTrue();
            assertThat(result.get().getOrderNum()).isEqualTo(7L);
            assertThat(result.get().getStatus()).isEqualTo(EntityStatus.ACTIVE);
            assertThat(result.get().getProductTags()).isEmpty();
        }
    }

    @Nested
    @DisplayName("mapToDomainWithoutPolicyProduct")
    class MapToDomainWithoutPolicyProduct {

        @Test
        @DisplayName("엔티티가 null이면 empty를 반환한다")
        void returnsEmptyWhenNull() {
            Optional<Product> result = ProductJpaEntityToDomainMapper.mapToDomainWithoutPolicyProduct(null);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("defaultPricePolicy 없이 필드를 매핑한다")
        void mapsWithoutPolicy() {
            ProductJpaEntity entity = productMock(UUID.randomUUID());

            Optional<Product> result = ProductJpaEntityToDomainMapper.mapToDomainWithoutPolicyProduct(entity);

            assertThat(result).isPresent();
            assertThat(result.get().getDefaultPricePolicy()).isNull();
        }
    }

    @Nested
    @DisplayName("mapToDomain(ProductOptionJpaEntity)")
    class MapToDomainOption {

        @Test
        @DisplayName("옵션 엔티티가 null이면 empty를 반환한다")
        void returnsEmptyWhenNull() {
            Optional<ProductOption> result = ProductJpaEntityToDomainMapper.mapToDomain((ProductOptionJpaEntity) null);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("mapToDomain(ProductOptionCategoryJpaEntity)")
    class MapToDomainCategory {

        @Test
        @DisplayName("카테고리 엔티티가 null이면 empty를 반환한다")
        void returnsEmptyWhenNull() {
            Optional<ProductOptionCategory> result = ProductJpaEntityToDomainMapper.mapToDomain((ProductOptionCategoryJpaEntity) null);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("mapToDomainWithoutProductTags(ProductOptionCategoryJpaEntity)")
    class MapToDomainWithoutProductTagsCategory {

        @Test
        @DisplayName("카테고리 엔티티가 null이면 empty를 반환한다")
        void returnsEmptyWhenNull() {
            Optional<ProductOptionCategory> result = ProductJpaEntityToDomainMapper.mapToDomainWithoutProductTags(null);

            assertThat(result).isEmpty();
        }
    }

    private ProductJpaEntity productMock(UUID productKey) {
        ProductJpaEntity entity = mock(ProductJpaEntity.class);
        when(entity.getId()).thenReturn(1L);
        when(entity.getProductKey()).thenReturn(productKey);
        when(entity.getSellerId()).thenReturn(10L);
        when(entity.getName()).thenReturn("상품명");
        when(entity.getBrandName()).thenReturn("브랜드");
        when(entity.getDetail()).thenReturn("상세");
        when(entity.getSales()).thenReturn(5);
        when(entity.getViewCount()).thenReturn(100L);
        when(entity.getPopularity()).thenReturn(50L);
        when(entity.isFindAllOptionsYn()).thenReturn(true);
        when(entity.getOrderNum()).thenReturn(7L);
        when(entity.getStatus()).thenReturn(EntityStatus.ACTIVE);
        when(entity.getProductTagJpaEntities()).thenReturn(List.of());
        when(entity.getDefaultPricePolicy()).thenReturn(null);
        return entity;
    }
}
