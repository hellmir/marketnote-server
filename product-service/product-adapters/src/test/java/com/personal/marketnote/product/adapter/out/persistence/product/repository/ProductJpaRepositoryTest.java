package com.personal.marketnote.product.adapter.out.persistence.product.repository;

import com.personal.marketnote.common.configuration.AuditConfig;
import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.common.utility.RandomCodeGenerator;
import com.personal.marketnote.product.adapter.out.persistence.pricepolicy.entity.PricePolicyJpaEntity;
import com.personal.marketnote.product.adapter.out.persistence.pricepolicy.repository.PricePolicyJpaRepository;
import com.personal.marketnote.product.adapter.out.persistence.product.entity.ProductJpaEntity;
import com.personal.marketnote.product.adapter.out.persistence.productcategory.entity.ProductCategoryJpaEntity;
import com.personal.marketnote.product.adapter.out.persistence.productcategory.repository.ProductCategoryJpaRepository;
import com.personal.marketnote.product.domain.pricepolicy.PricePolicy;
import com.personal.marketnote.product.domain.pricepolicy.PricePolicyCreateState;
import com.personal.marketnote.product.domain.pricepolicy.Rate;
import com.personal.marketnote.product.domain.product.Product;
import com.personal.marketnote.product.domain.product.ProductSnapshotState;
import com.personal.marketnote.product.domain.product.ProductTag;
import com.personal.marketnote.product.domain.product.ProductTagSnapshotState;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(AuditConfig.class)
class ProductJpaRepositoryTest {

    @Autowired
    private ProductJpaRepository productJpaRepository;

    @Autowired
    private ProductCategoryJpaRepository productCategoryJpaRepository;

    @Autowired
    private PricePolicyJpaRepository pricePolicyJpaRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Nested
    @DisplayName("existsByIdAndSellerId")
    class ExistsByIdAndSellerId {

        @Test
        @DisplayName("상품 ID와 판매자 ID가 일치하면 true를 반환한다")
        void returnsTrueWhenMatches() {
            ProductJpaEntity product = saveProduct("상품", "브랜드", 10L);

            boolean result = productJpaRepository.existsByIdAndSellerId(product.getId(), 10L);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("판매자 ID가 다르면 false를 반환한다")
        void returnsFalseWhenSellerDiffers() {
            ProductJpaEntity product = saveProduct("상품", "브랜드", 10L);

            boolean result = productJpaRepository.existsByIdAndSellerId(product.getId(), 99L);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("상품이 존재하지 않으면 false를 반환한다")
        void returnsFalseWhenProductNotExists() {
            boolean result = productJpaRepository.existsByIdAndSellerId(999L, 10L);

            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("findAllByCategoryIdOrderByOrderNumAsc")
    class FindAllByCategoryId {

        @Test
        @DisplayName("카테고리에 속한 상품을 orderNum 오름차순으로 조회한다")
        void returnsProductsInCategoryOrderByOrderNumAsc() {
            ProductJpaEntity product1 = saveProduct("상품1", "브랜드", 1L);
            ProductJpaEntity product2 = saveProduct("상품2", "브랜드", 1L);
            ProductJpaEntity product3 = saveProduct("상품3", "브랜드", 1L);
            Long categoryId = 500L;
            productCategoryJpaRepository.save(ProductCategoryJpaEntity.of(product1.getId(), categoryId));
            productCategoryJpaRepository.save(ProductCategoryJpaEntity.of(product2.getId(), categoryId));
            productCategoryJpaRepository.save(ProductCategoryJpaEntity.of(product3.getId(), categoryId));
            entityManager.flush();
            entityManager.clear();

            List<ProductJpaEntity> results = productJpaRepository.findAllByCategoryIdOrderByOrderNumAsc(categoryId);

            assertThat(results).extracting(ProductJpaEntity::getId)
                    .containsExactly(product1.getId(), product2.getId(), product3.getId());
        }

        @Test
        @DisplayName("카테고리에 속한 상품이 없으면 빈 목록을 반환한다")
        void returnsEmptyWhenNoProductsInCategory() {
            List<ProductJpaEntity> results = productJpaRepository.findAllByCategoryIdOrderByOrderNumAsc(999L);

            assertThat(results).isEmpty();
        }

        @Test
        @DisplayName("다른 카테고리의 상품은 포함하지 않는다")
        void excludesOtherCategories() {
            ProductJpaEntity product1 = saveProduct("상품1", "브랜드", 1L);
            ProductJpaEntity product2 = saveProduct("상품2", "브랜드", 1L);
            productCategoryJpaRepository.save(ProductCategoryJpaEntity.of(product1.getId(), 100L));
            productCategoryJpaRepository.save(ProductCategoryJpaEntity.of(product2.getId(), 200L));
            entityManager.flush();
            entityManager.clear();

            List<ProductJpaEntity> results = productJpaRepository.findAllByCategoryIdOrderByOrderNumAsc(100L);

            assertThat(results).hasSize(1);
            assertThat(results.getFirst().getId()).isEqualTo(product1.getId());
        }
    }

    @Nested
    @DisplayName("findByPricePolicyIds")
    class FindByPricePolicyIds {

        @Test
        @DisplayName("가격 정책 ID 목록에 해당하는 ACTIVE 상품을 조회한다")
        void returnsActiveProductsByPolicyIds() {
            ProductJpaEntity product1 = saveProduct("상품1", "브랜드", 1L);
            ProductJpaEntity product2 = saveProduct("상품2", "브랜드", 1L);
            PricePolicyJpaEntity policy1 = savePricePolicy(product1);
            PricePolicyJpaEntity policy2 = savePricePolicy(product2);
            entityManager.flush();
            entityManager.clear();

            List<ProductJpaEntity> results = productJpaRepository
                    .findByPricePolicyIds(List.of(policy1.getId(), policy2.getId()));

            assertThat(results).extracting(ProductJpaEntity::getId)
                    .containsExactlyInAnyOrder(product1.getId(), product2.getId());
        }

        @Test
        @DisplayName("INACTIVE 상품은 제외한다")
        void excludesInactiveProducts() {
            ProductJpaEntity active = saveProduct("활성 상품", "브랜드", 1L);
            ProductJpaEntity inactive = saveProduct("비활성 상품", "브랜드", 1L);
            PricePolicyJpaEntity activePolicy = savePricePolicy(active);
            PricePolicyJpaEntity inactivePolicy = savePricePolicy(inactive);
            entityManager.createNativeQuery("UPDATE product SET status = 'INACTIVE' WHERE id = :id")
                    .setParameter("id", inactive.getId())
                    .executeUpdate();
            entityManager.flush();
            entityManager.clear();

            List<ProductJpaEntity> results = productJpaRepository
                    .findByPricePolicyIds(List.of(activePolicy.getId(), inactivePolicy.getId()));

            assertThat(results).hasSize(1);
            assertThat(results.getFirst().getId()).isEqualTo(active.getId());
        }
    }

    @Nested
    @DisplayName("findAllWithTagsByIdIn / findAllWithPricePoliciesByIdIn")
    class FindAllWithTagsAndPricePolicies {

        @Test
        @DisplayName("상품 ID 목록에 대해 태그와 가격 정책을 각각 fetch 하면 MultipleBagFetchException 없이 조회된다")
        void returnsProductsWithTagsAndPricePoliciesWithoutMultipleBagFetchException() {
            ProductJpaEntity product1 = saveProduct("상품1", "브랜드", 1L, List.of("태그A", "태그B"));
            ProductJpaEntity product2 = saveProduct("상품2", "브랜드", 1L, List.of("태그C"));
            savePricePolicy(product1);
            savePricePolicy(product1);
            savePricePolicy(product2);
            entityManager.flush();
            entityManager.clear();

            List<Long> ids = List.of(product1.getId(), product2.getId());
            List<ProductJpaEntity> withTags = productJpaRepository.findAllWithTagsByIdIn(ids);
            productJpaRepository.findAllWithPricePoliciesByIdIn(ids);

            assertThat(withTags).hasSize(2);
            ProductJpaEntity loadedProduct1 = withTags.stream()
                    .filter(p -> p.getId().equals(product1.getId()))
                    .findFirst()
                    .orElseThrow();
            ProductJpaEntity loadedProduct2 = withTags.stream()
                    .filter(p -> p.getId().equals(product2.getId()))
                    .findFirst()
                    .orElseThrow();
            assertThat(loadedProduct1.getProductTagJpaEntities()).hasSize(2);
            assertThat(loadedProduct1.getPricePolicyJpaEntities()).hasSize(2);
            assertThat(loadedProduct2.getProductTagJpaEntities()).hasSize(1);
            assertThat(loadedProduct2.getPricePolicyJpaEntities()).hasSize(1);
        }

        @Test
        @DisplayName("주어진 ID 목록이 비어 있으면 빈 목록을 반환한다")
        void returnsEmptyWhenIdsEmpty() {
            List<ProductJpaEntity> withTags = productJpaRepository.findAllWithTagsByIdIn(List.of());
            List<ProductJpaEntity> withPolicies = productJpaRepository.findAllWithPricePoliciesByIdIn(List.of());

            assertThat(withTags).isEmpty();
            assertThat(withPolicies).isEmpty();
        }
    }

    private ProductJpaEntity saveProduct(String name, String brandName, Long sellerId) {
        return saveProduct(name, brandName, sellerId, List.of());
    }

    private ProductJpaEntity saveProduct(String name, String brandName, Long sellerId, List<String> tagNames) {
        List<ProductTag> productTags = tagNames.stream()
                .map(tagName -> ProductTag.from(
                        ProductTagSnapshotState.builder()
                                .name(tagName)
                                .status(EntityStatus.ACTIVE)
                                .build()
                ))
                .toList();
        Product product = Product.from(
                ProductSnapshotState.builder()
                        .productKey(RandomCodeGenerator.generateProductKey())
                        .sellerId(sellerId)
                        .name(name)
                        .brandName(brandName)
                        .detail("설명")
                        .findAllOptionsYn(false)
                        .productTags(productTags)
                        .status(EntityStatus.ACTIVE)
                        .build()
        );
        ProductJpaEntity entity = ProductJpaEntity.from(product);
        ProductJpaEntity saved = productJpaRepository.save(entity);
        saved.setIdToOrderNum();
        return saved;
    }

    private PricePolicyJpaEntity savePricePolicy(ProductJpaEntity productEntity) {
        PricePolicy pricePolicy = PricePolicy.from(
                PricePolicyCreateState.builder()
                        .price(10000L)
                        .discountPrice(9000L)
                        .discountRate(Rate.of(BigDecimal.valueOf(10.0)))
                        .accumulatedPoint(100L)
                        .accumulationRate(Rate.of(BigDecimal.valueOf(1.0)))
                        .build()
        );
        PricePolicyJpaEntity entity = PricePolicyJpaEntity.from(productEntity, pricePolicy);
        PricePolicyJpaEntity saved = pricePolicyJpaRepository.save(entity);
        saved.setIdToOrderNum();
        return saved;
    }
}
