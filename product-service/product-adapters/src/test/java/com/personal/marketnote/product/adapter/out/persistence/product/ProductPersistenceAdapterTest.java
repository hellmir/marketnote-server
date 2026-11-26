package com.personal.marketnote.product.adapter.out.persistence.product;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.common.utility.RandomCodeGenerator;
import com.personal.marketnote.product.adapter.out.persistence.product.entity.ProductJpaEntity;
import com.personal.marketnote.product.adapter.out.persistence.product.repository.ProductJpaRepository;
import com.personal.marketnote.product.domain.product.FasstoProductCodeGenerator;
import com.personal.marketnote.product.domain.product.Product;
import com.personal.marketnote.product.domain.product.ProductSnapshotState;
import com.personal.marketnote.product.exception.DuplicateFasstoProductCodeException;
import com.personal.marketnote.product.exception.ProductNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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

        @Test
        @DisplayName("DataIntegrityViolationException 메시지에 fasstoProductCode 유니크 제약명이 포함되면 DuplicateFasstoProductCodeException을 던진다")
        void throwsDuplicateFasstoProductCodeWhenMessageContainsConstraintName() {
            Product product = buildProductDomain(null, "상품", EntityStatus.ACTIVE);
            when(productJpaRepository.save(any(ProductJpaEntity.class)))
                    .thenThrow(new DataIntegrityViolationException("could not execute statement [uk_product_fassto_product_code]"));

            assertThatThrownBy(() -> adapter.save(product))
                    .isInstanceOf(DuplicateFasstoProductCodeException.class)
                    .hasMessageContaining("이미 등록된 fasstoProductCode입니다");
        }

        @Test
        @DisplayName("DataIntegrityViolationException 루트 원인 메시지에 fasstoProductCode 유니크 제약명이 포함되면 DuplicateFasstoProductCodeException을 던진다")
        void throwsDuplicateFasstoProductCodeWhenRootCauseMessageContainsConstraintName() {
            Product product = buildProductDomain(null, "상품", EntityStatus.ACTIVE);
            DataIntegrityViolationException dive = new DataIntegrityViolationException(
                    "could not execute",
                    new RuntimeException("ERROR: duplicate key value violates unique constraint \"uk_product_fassto_product_code\"")
            );
            when(productJpaRepository.save(any(ProductJpaEntity.class))).thenThrow(dive);

            assertThatThrownBy(() -> adapter.save(product))
                    .isInstanceOf(DuplicateFasstoProductCodeException.class);
        }

        @Test
        @DisplayName("다른 유니크 제약 위반은 DataIntegrityViolationException을 그대로 전파한다")
        void rethrowsWhenOtherConstraintViolation() {
            Product product = buildProductDomain(null, "상품", EntityStatus.ACTIVE);
            DataIntegrityViolationException dive = new DataIntegrityViolationException(
                    "could not execute statement [uk_other_constraint]"
            );
            when(productJpaRepository.save(any(ProductJpaEntity.class))).thenThrow(dive);

            assertThatThrownBy(() -> adapter.save(product))
                    .isInstanceOf(DataIntegrityViolationException.class)
                    .isNotInstanceOf(DuplicateFasstoProductCodeException.class);
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
    @DisplayName("findAll")
    class FindAll {

        @Test
        @DisplayName("결과가 없으면 빈 리스트를 반환한다")
        void returnsEmptyListWhenNoProducts() {
            when(productJpaRepository.findAll()).thenReturn(List.of());

            List<Product> result = adapter.findAll();

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("hydrate된 상품을 도메인 리스트로 매핑하여 반환한다")
        void returnsHydratedDomains() {
            Product product = buildProductDomain(1L, "상품", EntityStatus.ACTIVE);
            ProductJpaEntity baseEntity = buildEntityWithId(1L, product);
            ProductJpaEntity hydratedEntity = buildEntityWithId(1L, product);

            when(productJpaRepository.findAll()).thenReturn(List.of(baseEntity));
            when(productJpaRepository.findAllWithTagsByIdIn(List.of(1L))).thenReturn(List.of(hydratedEntity));
            when(productJpaRepository.findAllWithPricePoliciesByIdIn(List.of(1L))).thenReturn(List.of(hydratedEntity));

            List<Product> result = adapter.findAll();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("hydrate 결과가 비면 빈 리스트를 반환한다")
        void returnsEmptyListWhenHydratedEmpty() {
            Product product = buildProductDomain(1L, "상품", EntityStatus.ACTIVE);
            ProductJpaEntity baseEntity = buildEntityWithId(1L, product);

            when(productJpaRepository.findAll()).thenReturn(List.of(baseEntity));
            when(productJpaRepository.findAllWithTagsByIdIn(List.of(1L))).thenReturn(List.of());

            List<Product> result = adapter.findAll();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findAllByCategoryId")
    class FindAllByCategoryId {

        @Test
        @DisplayName("결과가 없으면 빈 리스트를 반환한다")
        void returnsEmptyListWhenNoProducts() {
            when(productJpaRepository.findAllByCategoryIdOrderByOrderNumAsc(1L)).thenReturn(List.of());

            List<Product> result = adapter.findAllByCategoryId(1L);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("카테고리 상품을 hydrate하여 도메인 리스트로 반환한다")
        void returnsHydratedDomains() {
            Product product = buildProductDomain(2L, "상품", EntityStatus.ACTIVE);
            ProductJpaEntity baseEntity = buildEntityWithId(2L, product);
            ProductJpaEntity hydratedEntity = buildEntityWithId(2L, product);

            when(productJpaRepository.findAllByCategoryIdOrderByOrderNumAsc(7L)).thenReturn(List.of(baseEntity));
            when(productJpaRepository.findAllWithTagsByIdIn(List.of(2L))).thenReturn(List.of(hydratedEntity));
            when(productJpaRepository.findAllWithPricePoliciesByIdIn(List.of(2L))).thenReturn(List.of(hydratedEntity));

            List<Product> result = adapter.findAllByCategoryId(7L);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getId()).isEqualTo(2L);
        }
    }

    @Nested
    @DisplayName("findByPricePolicyIds")
    class FindByPricePolicyIds {

        @Test
        @DisplayName("결과가 없으면 빈 리스트를 반환한다")
        void returnsEmptyListWhenNoProducts() {
            when(productJpaRepository.findByPricePolicyIds(List.of(10L))).thenReturn(List.of());

            List<Product> result = adapter.findByPricePolicyIds(List.of(10L));

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("가격 정책 ID로 매칭된 상품을 hydrate하여 도메인 리스트로 반환한다")
        void returnsHydratedDomains() {
            Product product = buildProductDomain(3L, "상품", EntityStatus.ACTIVE);
            ProductJpaEntity baseEntity = buildEntityWithId(3L, product);
            ProductJpaEntity hydratedEntity = buildEntityWithId(3L, product);

            when(productJpaRepository.findByPricePolicyIds(List.of(10L))).thenReturn(List.of(baseEntity));
            when(productJpaRepository.findAllWithTagsByIdIn(List.of(3L))).thenReturn(List.of(hydratedEntity));
            when(productJpaRepository.findAllWithPricePoliciesByIdIn(List.of(3L))).thenReturn(List.of(hydratedEntity));

            List<Product> result = adapter.findByPricePolicyIds(List.of(10L));

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getId()).isEqualTo(3L);
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
        UUID productKey = RandomCodeGenerator.generateProductKey();
        return Product.from(
                ProductSnapshotState.builder()
                        .id(id)
                        .productKey(productKey)
                        .sellerId(1L)
                        .name(name)
                        .brandName("브랜드")
                        .detail("설명")
                        .findAllOptionsYn(false)
                        .productTags(List.of())
                        .status(status)
                        .fasstoProductCode(FasstoProductCodeGenerator.encode(productKey))
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
