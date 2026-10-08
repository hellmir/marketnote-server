package com.personal.marketnote.product.adapter.out.persistence.pricepolicy.repository;

import com.personal.marketnote.common.configuration.AuditConfig;
import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.common.utility.RandomCodeGenerator;
import com.personal.marketnote.product.adapter.out.persistence.pricepolicy.entity.PricePolicyJpaEntity;
import com.personal.marketnote.product.adapter.out.persistence.product.entity.ProductJpaEntity;
import com.personal.marketnote.product.adapter.out.persistence.product.repository.ProductJpaRepository;
import com.personal.marketnote.product.adapter.out.persistence.productoption.entity.ProductOptionCategoryJpaEntity;
import com.personal.marketnote.product.adapter.out.persistence.productoption.entity.ProductOptionJpaEntity;
import com.personal.marketnote.product.adapter.out.persistence.productoption.entity.ProductOptionPricePolicyJpaEntity;
import com.personal.marketnote.product.adapter.out.persistence.productoption.repository.ProductOptionCategoryJpaRepository;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(AuditConfig.class)
class PricePolicyJpaRepositoryTest {

    @Autowired
    private PricePolicyJpaRepository pricePolicyJpaRepository;

    @Autowired
    private ProductJpaRepository productJpaRepository;

    @Autowired
    private ProductOptionCategoryJpaRepository productOptionCategoryJpaRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Nested
    @DisplayName("findAllByProductJpaEntity_IdOrderByIdDesc")
    class FindAllByProductId {

        @Test
        @DisplayName("상품에 연결된 모든 가격 정책을 ID 내림차순으로 조회한다")
        void returnsAllPricePoliciesOrderByIdDesc() {
            ProductJpaEntity product = saveProduct("상품1", "브랜드1");
            PricePolicyJpaEntity first = savePricePolicy(product, 1000L);
            PricePolicyJpaEntity second = savePricePolicy(product, 2000L);
            PricePolicyJpaEntity third = savePricePolicy(product, 3000L);

            List<PricePolicyJpaEntity> results = pricePolicyJpaRepository
                    .findAllByProductJpaEntity_IdOrderByIdDesc(product.getId());

            assertThat(results).extracting(PricePolicyJpaEntity::getId)
                    .containsExactly(third.getId(), second.getId(), first.getId());
        }

        @Test
        @DisplayName("다른 상품의 가격 정책은 포함하지 않는다")
        void excludesOtherProducts() {
            ProductJpaEntity productA = saveProduct("상품A", "브랜드A");
            ProductJpaEntity productB = saveProduct("상품B", "브랜드B");
            PricePolicyJpaEntity policyA = savePricePolicy(productA, 1000L);
            savePricePolicy(productB, 2000L);

            List<PricePolicyJpaEntity> results = pricePolicyJpaRepository
                    .findAllByProductJpaEntity_IdOrderByIdDesc(productA.getId());

            assertThat(results).hasSize(1);
            assertThat(results.getFirst().getId()).isEqualTo(policyA.getId());
        }

        @Test
        @DisplayName("상품에 가격 정책이 없으면 빈 목록을 반환한다")
        void returnsEmptyWhenNoPolicies() {
            ProductJpaEntity product = saveProduct("상품1", "브랜드1");

            List<PricePolicyJpaEntity> results = pricePolicyJpaRepository
                    .findAllByProductJpaEntity_IdOrderByIdDesc(product.getId());

            assertThat(results).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByOptionIds")
    class FindByOptionIds {

        @Test
        @DisplayName("지정한 옵션 조합에 해당하는 가격 정책을 조회한다")
        void returnsPricePoliciesForOptionCombination() {
            ProductJpaEntity product = saveProduct("옵션 상품", "브랜드");
            ProductOptionCategoryJpaEntity category = saveOptionCategory(product, "색상");
            ProductOptionJpaEntity red = saveOption(category, "빨강");
            ProductOptionJpaEntity blue = saveOption(category, "파랑");
            PricePolicyJpaEntity policy = savePricePolicy(product, 1000L);
            mapOptionToPolicy(red, policy);
            mapOptionToPolicy(blue, policy);
            entityManager.flush();
            entityManager.clear();

            List<PricePolicyJpaEntity> results = pricePolicyJpaRepository
                    .findByOptionIds(List.of(red.getId(), blue.getId()));

            assertThat(results).hasSize(1);
            assertThat(results.getFirst().getId()).isEqualTo(policy.getId());
        }

        @Test
        @DisplayName("옵션 일부만 매칭되는 가격 정책은 제외된다")
        void excludesPartiallyMatchingPolicies() {
            ProductJpaEntity product = saveProduct("옵션 상품", "브랜드");
            ProductOptionCategoryJpaEntity category = saveOptionCategory(product, "색상");
            ProductOptionJpaEntity red = saveOption(category, "빨강");
            ProductOptionJpaEntity blue = saveOption(category, "파랑");
            PricePolicyJpaEntity redOnly = savePricePolicy(product, 1000L);
            mapOptionToPolicy(red, redOnly);
            entityManager.flush();
            entityManager.clear();

            List<PricePolicyJpaEntity> results = pricePolicyJpaRepository
                    .findByOptionIds(List.of(red.getId(), blue.getId()));

            assertThat(results).isEmpty();
        }
    }

    @Nested
    @DisplayName("findOneByOptionIds")
    class FindOneByOptionIds {

        @Test
        @DisplayName("옵션 조합에 해당하는 가격 정책 중 가장 최근 ID를 반환한다")
        void returnsLatestPolicyForOptionCombination() {
            ProductJpaEntity product = saveProduct("옵션 상품", "브랜드");
            ProductOptionCategoryJpaEntity category = saveOptionCategory(product, "색상");
            ProductOptionJpaEntity red = saveOption(category, "빨강");
            PricePolicyJpaEntity older = savePricePolicy(product, 1000L);
            PricePolicyJpaEntity newer = savePricePolicy(product, 2000L);
            mapOptionToPolicy(red, older);
            mapOptionToPolicy(red, newer);
            entityManager.flush();
            entityManager.clear();

            Optional<PricePolicyJpaEntity> result = pricePolicyJpaRepository
                    .findOneByOptionIds(List.of(red.getId()));

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(newer.getId());
        }

        @Test
        @DisplayName("매칭되는 가격 정책이 없으면 빈 Optional을 반환한다")
        void returnsEmptyWhenNoMatch() {
            ProductJpaEntity product = saveProduct("옵션 상품", "브랜드");
            ProductOptionCategoryJpaEntity category = saveOptionCategory(product, "색상");
            ProductOptionJpaEntity red = saveOption(category, "빨강");
            entityManager.flush();
            entityManager.clear();

            Optional<PricePolicyJpaEntity> result = pricePolicyJpaRepository
                    .findOneByOptionIds(List.of(red.getId()));

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findAllActiveByOffsetAsc")
    class FindAllActiveByOffsetAsc {

        @Test
        @DisplayName("오프셋 기반 오름차순 정렬로 조회한다")
        void returnsResultsByOrderNumAsc() {
            ProductJpaEntity product1 = saveProduct("상품1", "브랜드1");
            ProductJpaEntity product2 = saveProduct("상품2", "브랜드2");
            ProductJpaEntity product3 = saveProduct("상품3", "브랜드3");
            PricePolicyJpaEntity policy1 = savePricePolicy(product1, 1000L);
            PricePolicyJpaEntity policy2 = savePricePolicy(product2, 2000L);
            PricePolicyJpaEntity policy3 = savePricePolicy(product3, 3000L);

            Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "orderNum"));

            List<PricePolicyJpaEntity> results = pricePolicyJpaRepository
                    .findAllActiveByOffsetAsc(null, pageable, "orderNum", "name", "", null);

            assertThat(results).extracting(PricePolicyJpaEntity::getId)
                    .containsExactly(policy1.getId(), policy2.getId(), policy3.getId());
        }

        @Test
        @DisplayName("페이지 크기만큼 결과를 제한한다")
        void respectsPageSize() {
            ProductJpaEntity product1 = saveProduct("상품1", "브랜드1");
            ProductJpaEntity product2 = saveProduct("상품2", "브랜드2");
            ProductJpaEntity product3 = saveProduct("상품3", "브랜드3");
            savePricePolicy(product1, 1000L);
            savePricePolicy(product2, 2000L);
            savePricePolicy(product3, 3000L);

            Pageable pageable = PageRequest.of(0, 2, Sort.by(Sort.Direction.ASC, "orderNum"));

            List<PricePolicyJpaEntity> results = pricePolicyJpaRepository
                    .findAllActiveByOffsetAsc(null, pageable, "orderNum", "name", "", null);

            assertThat(results).hasSize(2);
        }
    }

    @Nested
    @DisplayName("findAllActiveByOffsetDesc")
    class FindAllActiveByOffsetDesc {

        @Test
        @DisplayName("오프셋 기반 내림차순 정렬로 조회한다")
        void returnsResultsByOrderNumDesc() {
            ProductJpaEntity product1 = saveProduct("상품1", "브랜드1");
            ProductJpaEntity product2 = saveProduct("상품2", "브랜드2");
            ProductJpaEntity product3 = saveProduct("상품3", "브랜드3");
            PricePolicyJpaEntity policy1 = savePricePolicy(product1, 1000L);
            PricePolicyJpaEntity policy2 = savePricePolicy(product2, 2000L);
            PricePolicyJpaEntity policy3 = savePricePolicy(product3, 3000L);

            Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "orderNum"));

            List<PricePolicyJpaEntity> results = pricePolicyJpaRepository
                    .findAllActiveByOffsetDesc(null, pageable, "orderNum", "name", "", null);

            assertThat(results).extracting(PricePolicyJpaEntity::getId)
                    .containsExactly(policy3.getId(), policy2.getId(), policy1.getId());
        }

        @Test
        @DisplayName("pricePolicyIds 지정 시 해당 ID만 조회한다")
        void filtersByPricePolicyIds() {
            ProductJpaEntity product1 = saveProduct("상품1", "브랜드1");
            ProductJpaEntity product2 = saveProduct("상품2", "브랜드2");
            PricePolicyJpaEntity policy1 = savePricePolicy(product1, 1000L);
            savePricePolicy(product2, 2000L);

            Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "orderNum"));

            List<PricePolicyJpaEntity> results = pricePolicyJpaRepository
                    .findAllActiveByOffsetDesc(List.of(policy1.getId()), pageable, "orderNum", "name", "", null);

            assertThat(results).hasSize(1);
            assertThat(results.getFirst().getId()).isEqualTo(policy1.getId());
        }
    }

    @Nested
    @DisplayName("findAllWithProductAndOptionMappingsByIdIn")
    class FindAllWithProductAndOptionMappings {

        @Test
        @DisplayName("지정한 가격 정책 ID 목록의 연관 데이터를 JOIN FETCH로 조회한다")
        void returnsWithJoinFetch() {
            ProductJpaEntity product = saveProduct("상품", "브랜드");
            ProductOptionCategoryJpaEntity category = saveOptionCategory(product, "색상");
            ProductOptionJpaEntity red = saveOption(category, "빨강");
            PricePolicyJpaEntity policy = savePricePolicy(product, 1000L);
            mapOptionToPolicy(red, policy);
            entityManager.flush();
            entityManager.clear();

            List<PricePolicyJpaEntity> results = pricePolicyJpaRepository
                    .findAllWithProductAndOptionMappingsByIdIn(List.of(policy.getId()));

            assertThat(results).hasSize(1);
            PricePolicyJpaEntity fetched = results.getFirst();
            assertThat(fetched.getProductJpaEntity().getName()).isEqualTo("상품");
            assertThat(fetched.getProductOptionPricePolicyJpaEntities()).hasSize(1);
        }

        @Test
        @DisplayName("빈 ID 목록이면 빈 결과를 반환한다")
        void returnsEmptyForEmptyInput() {
            ProductJpaEntity product = saveProduct("상품", "브랜드");
            PricePolicyJpaEntity policy = savePricePolicy(product, 1000L);
            entityManager.flush();
            entityManager.clear();

            List<PricePolicyJpaEntity> results = pricePolicyJpaRepository
                    .findAllWithProductAndOptionMappingsByIdIn(List.of(policy.getId() + 100));

            assertThat(results).isEmpty();
        }
    }

    @Nested
    @DisplayName("deactivateByProductId")
    class DeactivateByProductId {

        @Test
        @DisplayName("상품에 연결된 모든 가격 정책을 INACTIVE로 변경한다")
        void deactivatesAllPoliciesForProduct() {
            ProductJpaEntity product = saveProduct("상품", "브랜드");
            PricePolicyJpaEntity first = savePricePolicy(product, 1000L);
            PricePolicyJpaEntity second = savePricePolicy(product, 2000L);

            pricePolicyJpaRepository.deactivateByProductId(product.getId());
            entityManager.clear();

            PricePolicyJpaEntity firstReloaded = pricePolicyJpaRepository.findById(first.getId()).orElseThrow();
            PricePolicyJpaEntity secondReloaded = pricePolicyJpaRepository.findById(second.getId()).orElseThrow();
            assertThat(firstReloaded.getStatus()).isEqualTo(EntityStatus.INACTIVE);
            assertThat(secondReloaded.getStatus()).isEqualTo(EntityStatus.INACTIVE);
        }

        @Test
        @DisplayName("다른 상품의 가격 정책은 영향을 받지 않는다")
        void doesNotAffectOtherProducts() {
            ProductJpaEntity productA = saveProduct("상품A", "브랜드A");
            ProductJpaEntity productB = saveProduct("상품B", "브랜드B");
            PricePolicyJpaEntity policyA = savePricePolicy(productA, 1000L);
            PricePolicyJpaEntity policyB = savePricePolicy(productB, 2000L);

            pricePolicyJpaRepository.deactivateByProductId(productA.getId());
            entityManager.clear();

            PricePolicyJpaEntity policyAReloaded = pricePolicyJpaRepository.findById(policyA.getId()).orElseThrow();
            PricePolicyJpaEntity policyBReloaded = pricePolicyJpaRepository.findById(policyB.getId()).orElseThrow();
            assertThat(policyAReloaded.getStatus()).isEqualTo(EntityStatus.INACTIVE);
            assertThat(policyBReloaded.getStatus()).isEqualTo(EntityStatus.ACTIVE);
        }
    }

    @Nested
    @DisplayName("정렬 속성별 커서 조회")
    class SortPropertyCursor {

        @Test
        @DisplayName("popularity 기준 내림차순으로 정렬한다")
        void sortsByPopularityDesc() {
            ProductJpaEntity productLow = saveProduct("낮은 인기", "브랜드");
            ProductJpaEntity productHigh = saveProduct("높은 인기", "브랜드");
            PricePolicyJpaEntity low = savePricePolicy(productLow, 1000L);
            PricePolicyJpaEntity high = savePricePolicy(productHigh, 1000L);
            updatePopularity(low.getId(), 10L);
            updatePopularity(high.getId(), 100L);
            entityManager.clear();

            Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "popularity"));

            List<PricePolicyJpaEntity> results = pricePolicyJpaRepository.findAllActiveByCursorDesc(
                    null, null, pageable, "popularity", "name", "", null
            );

            assertThat(results).extracting(PricePolicyJpaEntity::getId)
                    .containsExactly(high.getId(), low.getId());
        }

        @Test
        @DisplayName("discountPrice 기준 오름차순으로 정렬한다")
        void sortsByDiscountPriceAsc() {
            ProductJpaEntity product1 = saveProduct("상품1", "브랜드");
            ProductJpaEntity product2 = saveProduct("상품2", "브랜드");
            PricePolicyJpaEntity cheap = savePricePolicyWithDiscountPrice(product1, 1000L);
            PricePolicyJpaEntity expensive = savePricePolicyWithDiscountPrice(product2, 9000L);

            Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "discountPrice"));

            List<PricePolicyJpaEntity> results = pricePolicyJpaRepository.findAllActiveByCursorAsc(
                    null, null, pageable, "discountPrice", "name", "", null
            );

            assertThat(results).extracting(PricePolicyJpaEntity::getId)
                    .containsExactly(cheap.getId(), expensive.getId());
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

    private PricePolicyJpaEntity savePricePolicy(ProductJpaEntity productEntity, Long accumulatedPoint) {
        PricePolicy pricePolicy = PricePolicy.from(
                PricePolicyCreateState.builder()
                        .price(10000L)
                        .discountPrice(9000L)
                        .discountRate(Rate.of(BigDecimal.valueOf(10.0)))
                        .accumulatedPoint(accumulatedPoint)
                        .accumulationRate(Rate.of(BigDecimal.valueOf(1.0)))
                        .build()
        );
        PricePolicyJpaEntity entity = PricePolicyJpaEntity.from(productEntity, pricePolicy);
        PricePolicyJpaEntity saved = pricePolicyJpaRepository.save(entity);
        saved.setIdToOrderNum();
        return saved;
    }

    private PricePolicyJpaEntity savePricePolicyWithDiscountPrice(ProductJpaEntity productEntity, Long discountPrice) {
        PricePolicy pricePolicy = PricePolicy.from(
                PricePolicyCreateState.builder()
                        .price(10000L)
                        .discountPrice(discountPrice)
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

    private void updatePopularity(Long policyId, Long popularity) {
        entityManager.createNativeQuery("UPDATE price_policy SET popularity = :popularity WHERE id = :id")
                .setParameter("popularity", popularity)
                .setParameter("id", policyId)
                .executeUpdate();
    }
}
