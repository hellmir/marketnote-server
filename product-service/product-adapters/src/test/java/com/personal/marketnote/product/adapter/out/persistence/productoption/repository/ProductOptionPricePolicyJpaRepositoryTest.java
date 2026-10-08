package com.personal.marketnote.product.adapter.out.persistence.productoption.repository;

import com.personal.marketnote.common.configuration.AuditConfig;
import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.common.utility.RandomCodeGenerator;
import com.personal.marketnote.product.adapter.out.persistence.pricepolicy.entity.PricePolicyJpaEntity;
import com.personal.marketnote.product.adapter.out.persistence.pricepolicy.repository.PricePolicyJpaRepository;
import com.personal.marketnote.product.adapter.out.persistence.product.entity.ProductJpaEntity;
import com.personal.marketnote.product.adapter.out.persistence.product.repository.ProductJpaRepository;
import com.personal.marketnote.product.adapter.out.persistence.productoption.entity.ProductOptionCategoryJpaEntity;
import com.personal.marketnote.product.adapter.out.persistence.productoption.entity.ProductOptionJpaEntity;
import com.personal.marketnote.product.adapter.out.persistence.productoption.entity.ProductOptionPricePolicyJpaEntity;
import com.personal.marketnote.product.domain.option.ProductOption;
import com.personal.marketnote.product.domain.option.ProductOptionCategory;
import com.personal.marketnote.product.domain.option.ProductOptionCategoryCreateState;
import com.personal.marketnote.product.domain.option.ProductOptionCreateState;
import com.personal.marketnote.product.domain.pricepolicy.PricePolicy;
import com.personal.marketnote.product.domain.pricepolicy.PricePolicyCreateState;
import com.personal.marketnote.product.domain.pricepolicy.Rate;
import com.personal.marketnote.product.domain.product.Product;
import com.personal.marketnote.product.domain.product.ProductSnapshotState;
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
class ProductOptionPricePolicyJpaRepositoryTest {

    @Autowired
    private ProductOptionPricePolicyJpaRepository repository;

    @Autowired
    private PricePolicyJpaRepository pricePolicyJpaRepository;

    @Autowired
    private ProductJpaRepository productJpaRepository;

    @Autowired
    private ProductOptionCategoryJpaRepository productOptionCategoryJpaRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Nested
    @DisplayName("findCandidatePricePolicyIds")
    class FindCandidatePricePolicyIds {

        @Test
        @DisplayName("모든 옵션이 매칭되는 가격 정책 ID를 반환한다")
        void returnsFullyMatchingPolicyIds() {
            ProductJpaEntity product = saveProduct("상품", "브랜드");
            ProductOptionCategoryJpaEntity category = saveOptionCategory(product, "색상");
            ProductOptionJpaEntity red = saveOption(category, "빨강");
            ProductOptionJpaEntity blue = saveOption(category, "파랑");
            PricePolicyJpaEntity fullPolicy = savePricePolicy(product);
            mapOptionToPolicy(red, fullPolicy);
            mapOptionToPolicy(blue, fullPolicy);
            entityManager.flush();
            entityManager.clear();

            List<Long> results = repository.findCandidatePricePolicyIds(
                    List.of(red.getId(), blue.getId()), 2L
            );

            assertThat(results).containsExactly(fullPolicy.getId());
        }

        @Test
        @DisplayName("옵션 일부만 매칭되는 정책은 제외한다")
        void excludesPartiallyMatchingPolicies() {
            ProductJpaEntity product = saveProduct("상품", "브랜드");
            ProductOptionCategoryJpaEntity category = saveOptionCategory(product, "색상");
            ProductOptionJpaEntity red = saveOption(category, "빨강");
            ProductOptionJpaEntity blue = saveOption(category, "파랑");
            PricePolicyJpaEntity redOnly = savePricePolicy(product);
            mapOptionToPolicy(red, redOnly);
            entityManager.flush();
            entityManager.clear();

            List<Long> results = repository.findCandidatePricePolicyIds(
                    List.of(red.getId(), blue.getId()), 2L
            );

            assertThat(results).isEmpty();
        }
    }

    @Nested
    @DisplayName("countByPricePolicyJpaEntity_Id")
    class CountByPricePolicyId {

        @Test
        @DisplayName("가격 정책에 연결된 옵션 매핑 개수를 반환한다")
        void returnsMappingCount() {
            ProductJpaEntity product = saveProduct("상품", "브랜드");
            ProductOptionCategoryJpaEntity category = saveOptionCategory(product, "색상");
            ProductOptionJpaEntity red = saveOption(category, "빨강");
            ProductOptionJpaEntity blue = saveOption(category, "파랑");
            ProductOptionJpaEntity green = saveOption(category, "초록");
            PricePolicyJpaEntity policy = savePricePolicy(product);
            mapOptionToPolicy(red, policy);
            mapOptionToPolicy(blue, policy);
            mapOptionToPolicy(green, policy);
            entityManager.flush();
            entityManager.clear();

            long count = repository.countByPricePolicyJpaEntity_Id(policy.getId());

            assertThat(count).isEqualTo(3L);
        }

        @Test
        @DisplayName("매핑이 없으면 0을 반환한다")
        void returnsZeroWhenNoMappings() {
            ProductJpaEntity product = saveProduct("상품", "브랜드");
            PricePolicyJpaEntity policy = savePricePolicy(product);
            entityManager.flush();
            entityManager.clear();

            long count = repository.countByPricePolicyJpaEntity_Id(policy.getId());

            assertThat(count).isZero();
        }
    }

    @Nested
    @DisplayName("findOptionIdsByPricePolicyId")
    class FindOptionIdsByPricePolicyId {

        @Test
        @DisplayName("가격 정책에 매핑된 옵션 ID 목록을 반환한다")
        void returnsMappedOptionIds() {
            ProductJpaEntity product = saveProduct("상품", "브랜드");
            ProductOptionCategoryJpaEntity category = saveOptionCategory(product, "색상");
            ProductOptionJpaEntity red = saveOption(category, "빨강");
            ProductOptionJpaEntity blue = saveOption(category, "파랑");
            PricePolicyJpaEntity policy = savePricePolicy(product);
            mapOptionToPolicy(red, policy);
            mapOptionToPolicy(blue, policy);
            entityManager.flush();
            entityManager.clear();

            List<Long> optionIds = repository.findOptionIdsByPricePolicyId(policy.getId());

            assertThat(optionIds).containsExactlyInAnyOrder(red.getId(), blue.getId());
        }

        @Test
        @DisplayName("매핑이 없는 정책은 빈 목록을 반환한다")
        void returnsEmptyWhenNoMappings() {
            ProductJpaEntity product = saveProduct("상품", "브랜드");
            PricePolicyJpaEntity policy = savePricePolicy(product);
            entityManager.flush();
            entityManager.clear();

            List<Long> optionIds = repository.findOptionIdsByPricePolicyId(policy.getId());

            assertThat(optionIds).isEmpty();
        }
    }

    @Nested
    @DisplayName("deleteByOptionIds")
    class DeleteByOptionIds {

        @Test
        @DisplayName("지정한 옵션 ID에 매핑된 모든 가격 정책 연결을 삭제한다")
        void deletesMappingsByOptionIds() {
            ProductJpaEntity product = saveProduct("상품", "브랜드");
            ProductOptionCategoryJpaEntity category = saveOptionCategory(product, "색상");
            ProductOptionJpaEntity red = saveOption(category, "빨강");
            ProductOptionJpaEntity blue = saveOption(category, "파랑");
            PricePolicyJpaEntity policy = savePricePolicy(product);
            mapOptionToPolicy(red, policy);
            mapOptionToPolicy(blue, policy);
            entityManager.flush();

            repository.deleteByOptionIds(List.of(red.getId()));
            entityManager.flush();
            entityManager.clear();

            List<Long> remaining = repository.findOptionIdsByPricePolicyId(policy.getId());
            assertThat(remaining).containsExactly(blue.getId());
        }
    }

    @Nested
    @DisplayName("deleteByPricePolicyId")
    class DeleteByPricePolicyId {

        @Test
        @DisplayName("지정한 가격 정책의 모든 옵션 매핑을 삭제한다")
        void deletesAllMappingsOfPolicy() {
            ProductJpaEntity product = saveProduct("상품", "브랜드");
            ProductOptionCategoryJpaEntity category = saveOptionCategory(product, "색상");
            ProductOptionJpaEntity red = saveOption(category, "빨강");
            ProductOptionJpaEntity blue = saveOption(category, "파랑");
            PricePolicyJpaEntity policy = savePricePolicy(product);
            mapOptionToPolicy(red, policy);
            mapOptionToPolicy(blue, policy);
            entityManager.flush();

            repository.deleteByPricePolicyId(policy.getId());
            entityManager.flush();
            entityManager.clear();

            assertThat(repository.countByPricePolicyJpaEntity_Id(policy.getId())).isZero();
        }
    }

    @Nested
    @DisplayName("deleteByPricePolicyIds")
    class DeleteByPricePolicyIds {

        @Test
        @DisplayName("지정한 가격 정책 ID 목록의 모든 매핑을 삭제한다")
        void deletesAllMappingsByPolicyIds() {
            ProductJpaEntity product = saveProduct("상품", "브랜드");
            ProductOptionCategoryJpaEntity category = saveOptionCategory(product, "색상");
            ProductOptionJpaEntity red = saveOption(category, "빨강");
            PricePolicyJpaEntity policyA = savePricePolicy(product);
            PricePolicyJpaEntity policyB = savePricePolicy(product);
            mapOptionToPolicy(red, policyA);
            mapOptionToPolicy(red, policyB);
            entityManager.flush();

            repository.deleteByPricePolicyIds(List.of(policyA.getId(), policyB.getId()));
            entityManager.flush();
            entityManager.clear();

            assertThat(repository.countByPricePolicyJpaEntity_Id(policyA.getId())).isZero();
            assertThat(repository.countByPricePolicyJpaEntity_Id(policyB.getId())).isZero();
        }

        @Test
        @DisplayName("목록에 없는 정책의 매핑은 보존한다")
        void preservesNonTargetedPolicyMappings() {
            ProductJpaEntity product = saveProduct("상품", "브랜드");
            ProductOptionCategoryJpaEntity category = saveOptionCategory(product, "색상");
            ProductOptionJpaEntity red = saveOption(category, "빨강");
            PricePolicyJpaEntity targeted = savePricePolicy(product);
            PricePolicyJpaEntity preserved = savePricePolicy(product);
            mapOptionToPolicy(red, targeted);
            mapOptionToPolicy(red, preserved);
            entityManager.flush();

            repository.deleteByPricePolicyIds(List.of(targeted.getId()));
            entityManager.flush();
            entityManager.clear();

            assertThat(repository.countByPricePolicyJpaEntity_Id(targeted.getId())).isZero();
            assertThat(repository.countByPricePolicyJpaEntity_Id(preserved.getId())).isEqualTo(1L);
        }
    }

    private ProductJpaEntity saveProduct(String name, String brandName) {
        Product product = Product.from(
                ProductSnapshotState.builder()
                        .productKey(RandomCodeGenerator.generateProductKey())
                        .sellerId(1L)
                        .name(name)
                        .brandName(brandName)
                        .detail("설명")
                        .findAllOptionsYn(false)
                        .productTags(List.of())
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

    private ProductOptionCategoryJpaEntity saveOptionCategory(ProductJpaEntity product, String name) {
        ProductOptionCategoryCreateState state = ProductOptionCategoryCreateState.builder()
                .product(null)
                .name(name)
                .optionStates(List.of())
                .build();
        ProductOptionCategory category = ProductOptionCategory.from(state);
        ProductOptionCategoryJpaEntity entity = ProductOptionCategoryJpaEntity.from(category, product);
        ProductOptionCategoryJpaEntity saved = productOptionCategoryJpaRepository.save(entity);
        saved.setIdToOrderNum();
        return saved;
    }

    private ProductOptionJpaEntity saveOption(ProductOptionCategoryJpaEntity category, String content) {
        ProductOptionCreateState state = ProductOptionCreateState.builder()
                .category(null)
                .content(content)
                .build();
        ProductOption option = ProductOption.from(state);
        ProductOptionJpaEntity entity = ProductOptionJpaEntity.from(category, option);
        entityManager.persist(entity);
        entityManager.flush();
        entity.setIdToOrderNum();
        return entity;
    }

    private void mapOptionToPolicy(ProductOptionJpaEntity option, PricePolicyJpaEntity policy) {
        ProductOptionPricePolicyJpaEntity mapping = ProductOptionPricePolicyJpaEntity.of(option, policy);
        entityManager.persist(mapping);
    }
}
