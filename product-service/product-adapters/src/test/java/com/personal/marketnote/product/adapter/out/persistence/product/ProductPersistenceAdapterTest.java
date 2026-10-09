package com.personal.marketnote.product.adapter.out.persistence.product;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.common.utility.RandomCodeGenerator;
import com.personal.marketnote.product.adapter.out.persistence.product.entity.ProductJpaEntity;
import com.personal.marketnote.product.adapter.out.persistence.product.repository.ProductJpaRepository;
import com.personal.marketnote.product.domain.product.Product;
import com.personal.marketnote.product.domain.product.ProductSnapshotState;
import com.personal.marketnote.product.exception.ProductNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductPersistenceAdapterTest {

    @InjectMocks
    private ProductPersistenceAdapter adapter;

    @Mock
    private ProductJpaRepository productJpaRepository;

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("상품을 저장하면 저장된 도메인 객체를 반환한다")
        void savesAndReturnsDomain() {
            Product product = buildProductDomain(null, "상품", EntityStatus.ACTIVE);
            ProductJpaEntity savedEntity = buildEntityWithId(10L, product);
            when(productJpaRepository.save(any(ProductJpaEntity.class))).thenReturn(savedEntity);

            Product result = adapter.save(product);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(10L);
            verify(productJpaRepository).save(any(ProductJpaEntity.class));
        }
    }

    @Nested
    @DisplayName("existsByIdAndSellerId")
    class ExistsByIdAndSellerId {

        @Test
        @DisplayName("존재하면 true를 반환한다")
        void returnsTrueWhenExists() {
            when(productJpaRepository.existsByIdAndSellerId(1L, 2L)).thenReturn(true);

            boolean result = adapter.existsByIdAndSellerId(1L, 2L);

            assertThat(result).isTrue();
            verify(productJpaRepository).existsByIdAndSellerId(1L, 2L);
        }

        @Test
        @DisplayName("존재하지 않으면 false를 반환한다")
        void returnsFalseWhenNotExists() {
            when(productJpaRepository.existsByIdAndSellerId(1L, 2L)).thenReturn(false);

            boolean result = adapter.existsByIdAndSellerId(1L, 2L);

            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("ID로 조회 성공 시 도메인 객체를 반환한다")
        void returnsDomainWhenFound() {
            Product product = buildProductDomain(1L, "상품", EntityStatus.ACTIVE);
            ProductJpaEntity entity = buildEntityWithId(1L, product);
            when(productJpaRepository.findById(1L)).thenReturn(Optional.of(entity));

            Optional<Product> result = adapter.findById(1L);

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("조회 실패 시 Optional.empty를 반환한다")
        void returnsEmptyWhenNotFound() {
            when(productJpaRepository.findById(999L)).thenReturn(Optional.empty());

            Optional<Product> result = adapter.findById(999L);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findActiveById")
    class FindActiveById {

        @Test
        @DisplayName("ACTIVE 상태면 도메인 객체를 반환한다")
        void returnsDomainWhenActive() {
            Product product = buildProductDomain(1L, "상품", EntityStatus.ACTIVE);
            ProductJpaEntity entity = buildEntityWithId(1L, product);
            when(productJpaRepository.findById(1L)).thenReturn(Optional.of(entity));

            Optional<Product> result = adapter.findActiveById(1L);

            assertThat(result).isPresent();
        }

        @Test
        @DisplayName("INACTIVE 상태면 빈 Optional을 반환한다")
        void returnsEmptyWhenInactive() {
            Product product = buildProductDomain(1L, "상품", EntityStatus.INACTIVE);
            ProductJpaEntity entity = buildEntityWithId(1L, product);
            when(productJpaRepository.findById(1L)).thenReturn(Optional.of(entity));

            Optional<Product> result = adapter.findActiveById(1L);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("존재하는 상품을 업데이트한다")
        void updatesExistingProduct() {
            Product product = buildProductDomain(1L, "업데이트된 상품", EntityStatus.ACTIVE);
            ProductJpaEntity entity = buildEntityWithId(1L, buildProductDomain(1L, "기존 상품", EntityStatus.ACTIVE));
            when(productJpaRepository.findById(1L)).thenReturn(Optional.of(entity));

            adapter.update(product);

            verify(productJpaRepository).findById(1L);
            verify(productJpaRepository).flush();
        }

        @Test
        @DisplayName("상품이 없으면 ProductNotFoundException을 던진다")
        void throwsWhenNotFound() {
            Product product = buildProductDomain(999L, "상품", EntityStatus.ACTIVE);
            when(productJpaRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> adapter.update(product))
                    .isInstanceOf(ProductNotFoundException.class);
            verify(productJpaRepository, never()).flush();
        }
    }

    private Product buildProductDomain(Long id, String name, EntityStatus status) {
        return Product.from(
                ProductSnapshotState.builder()
                        .id(id)
                        .productKey(RandomCodeGenerator.generateProductKey())
                        .sellerId(1L)
                        .name(name)
                        .brandName("브랜드")
                        .detail("설명")
                        .findAllOptionsYn(false)
                        .productTags(List.of())
                        .status(status)
                        .build()
        );
    }

    private ProductJpaEntity buildEntityWithId(Long id, Product product) {
        ProductJpaEntity entity = ProductJpaEntity.from(product);
        ReflectionTestUtils.setField(entity, "id", id);
        ReflectionTestUtils.setField(entity, "status", product.getStatus());
        return entity;
    }
}
