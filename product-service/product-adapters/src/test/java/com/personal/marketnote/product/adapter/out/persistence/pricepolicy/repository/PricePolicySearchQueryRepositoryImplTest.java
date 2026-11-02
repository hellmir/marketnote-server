package com.personal.marketnote.product.adapter.out.persistence.pricepolicy.repository;

import com.personal.marketnote.common.configuration.AuditConfig;
import com.personal.marketnote.common.configuration.QuerydslConfig;
import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.common.utility.RandomCodeGenerator;
import com.personal.marketnote.product.adapter.out.persistence.pricepolicy.entity.PricePolicyJpaEntity;
import com.personal.marketnote.product.adapter.out.persistence.product.entity.ProductJpaEntity;
import com.personal.marketnote.product.adapter.out.persistence.product.repository.ProductJpaRepository;
import com.personal.marketnote.product.adapter.out.persistence.productcategory.entity.ProductCategoryJpaEntity;
import com.personal.marketnote.product.adapter.out.persistence.productcategory.repository.ProductCategoryJpaRepository;
import com.personal.marketnote.product.domain.pricepolicy.PricePolicy;
import com.personal.marketnote.product.domain.pricepolicy.PricePolicyCreateState;
import com.personal.marketnote.product.domain.pricepolicy.Rate;
import com.personal.marketnote.product.domain.product.Product;
import com.personal.marketnote.product.domain.product.ProductSearchTarget;
import com.personal.marketnote.product.domain.product.ProductSnapshotState;
import com.personal.marketnote.product.domain.product.ProductSortProperty;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
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
@Import({AuditConfig.class, QuerydslConfig.class})
class PricePolicySearchQueryRepositoryImplTest {

    @Autowired
    private PricePolicyJpaRepository pricePolicyJpaRepository;

    @Autowired
    private ProductJpaRepository productJpaRepository;

    @Autowired
    private ProductCategoryJpaRepository productCategoryJpaRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    @DisplayName("ORDER_NUM ASC 정렬로 오름차순 조회한다")
    void sortByOrderNumAsc() {
        ProductJpaEntity product1 = saveProduct("상품1", "브랜드1");
        ProductJpaEntity product2 = saveProduct("상품2", "브랜드2");
        ProductJpaEntity product3 = saveProduct("상품3", "브랜드3");
        PricePolicyJpaEntity policy1 = savePricePolicyBasic(product1);
        PricePolicyJpaEntity policy2 = savePricePolicyBasic(product2);
        PricePolicyJpaEntity policy3 = savePricePolicyBasic(product3);

        List<PricePolicyJpaEntity> results = pricePolicyJpaRepository.findActivePricePoliciesByOffset(
                null, 0, 10, true, ProductSortProperty.ORDER_NUM, ProductSearchTarget.NAME, null, null
        );

        assertThat(results).extracting(PricePolicyJpaEntity::getId)
                .containsExactly(policy1.getId(), policy2.getId(), policy3.getId());
    }

    @Test
    @DisplayName("ORDER_NUM DESC 정렬로 내림차순 조회한다")
    void sortByOrderNumDesc() {
        ProductJpaEntity product1 = saveProduct("상품1", "브랜드1");
        ProductJpaEntity product2 = saveProduct("상품2", "브랜드2");
        ProductJpaEntity product3 = saveProduct("상품3", "브랜드3");
        PricePolicyJpaEntity policy1 = savePricePolicyBasic(product1);
        PricePolicyJpaEntity policy2 = savePricePolicyBasic(product2);
        PricePolicyJpaEntity policy3 = savePricePolicyBasic(product3);

        List<PricePolicyJpaEntity> results = pricePolicyJpaRepository.findActivePricePoliciesByOffset(
                null, 0, 10, false, ProductSortProperty.ORDER_NUM, ProductSearchTarget.NAME, null, null
        );

        assertThat(results).extracting(PricePolicyJpaEntity::getId)
                .containsExactly(policy3.getId(), policy2.getId(), policy1.getId());
    }

    @Test
    @DisplayName("POPULARITY 정렬 시 popularity 컬럼으로 정렬된다")
    void sortByPopularity() {
        ProductJpaEntity product1 = saveProduct("상품1", "브랜드1");
        ProductJpaEntity product2 = saveProduct("상품2", "브랜드2");
        PricePolicyJpaEntity policyLow = savePricePolicyWithPopularity(product1, 10L);
        PricePolicyJpaEntity policyHigh = savePricePolicyWithPopularity(product2, 100L);

        List<PricePolicyJpaEntity> asc = pricePolicyJpaRepository.findActivePricePoliciesByOffset(
                null, 0, 10, true, ProductSortProperty.POPULARITY, ProductSearchTarget.NAME, null, null
        );

        assertThat(asc).extracting(PricePolicyJpaEntity::getId)
                .containsExactly(policyLow.getId(), policyHigh.getId());
    }

    @Test
    @DisplayName("DISCOUNT_PRICE 정렬 시 discountPrice 컬럼으로 정렬된다")
    void sortByDiscountPrice() {
        ProductJpaEntity product1 = saveProduct("상품1", "브랜드1");
        ProductJpaEntity product2 = saveProduct("상품2", "브랜드2");
        PricePolicyJpaEntity cheap = savePricePolicyWithDiscountPrice(product1, 1000L);
        PricePolicyJpaEntity expensive = savePricePolicyWithDiscountPrice(product2, 9000L);

        List<PricePolicyJpaEntity> asc = pricePolicyJpaRepository.findActivePricePoliciesByOffset(
                null, 0, 10, true, ProductSortProperty.DISCOUNT_PRICE, ProductSearchTarget.NAME, null, null
        );

        assertThat(asc).extracting(PricePolicyJpaEntity::getId)
                .containsExactly(cheap.getId(), expensive.getId());
    }

    @Test
    @DisplayName("ACCUMULATED_POINT 정렬 시 동점은 p.id DESC tiebreaker로 정렬된다")
    void accumulatedPointTiebreaker() {
        ProductJpaEntity product1 = saveProduct("상품1", "브랜드1");
        ProductJpaEntity product2 = saveProduct("상품2", "브랜드2");
        ProductJpaEntity product3 = saveProduct("상품3", "브랜드3");
        PricePolicyJpaEntity policy1 = savePricePolicyWithAccumulatedPoint(product1, 500L);
        PricePolicyJpaEntity policy2 = savePricePolicyWithAccumulatedPoint(product2, 500L);
        PricePolicyJpaEntity policy3 = savePricePolicyWithAccumulatedPoint(product3, 500L);

        List<PricePolicyJpaEntity> asc = pricePolicyJpaRepository.findActivePricePoliciesByOffset(
                null, 0, 10, true, ProductSortProperty.ACCUMULATED_POINT, ProductSearchTarget.NAME, null, null
        );

        assertThat(asc).extracting(PricePolicyJpaEntity::getId)
                .containsExactly(policy3.getId(), policy2.getId(), policy1.getId());
    }

    @Test
    @DisplayName("ACCUMULATED_POINT_RATE 정렬 시 동점은 p.id DESC tiebreaker로 정렬된다")
    void accumulatedPointRateTiebreaker() {
        ProductJpaEntity product1 = saveProduct("상품1", "브랜드1");
        ProductJpaEntity product2 = saveProduct("상품2", "브랜드2");
        ProductJpaEntity product3 = saveProduct("상품3", "브랜드3");
        PricePolicyJpaEntity policy1 = savePricePolicyWithAccumulationRate(product1, BigDecimal.valueOf(1.0));
        PricePolicyJpaEntity policy2 = savePricePolicyWithAccumulationRate(product2, BigDecimal.valueOf(1.0));
        PricePolicyJpaEntity policy3 = savePricePolicyWithAccumulationRate(product3, BigDecimal.valueOf(1.0));

        List<PricePolicyJpaEntity> asc = pricePolicyJpaRepository.findActivePricePoliciesByOffset(
                null, 0, 10, true, ProductSortProperty.ACCUMULATED_POINT_RATE, ProductSearchTarget.NAME, null, null
        );

        assertThat(asc).extracting(PricePolicyJpaEntity::getId)
                .containsExactly(policy3.getId(), policy2.getId(), policy1.getId());
    }

    @Test
    @DisplayName("pricePolicyIds 지정 시 해당 ID만 필터링된다")
    void filterByPricePolicyIds() {
        ProductJpaEntity product1 = saveProduct("상품1", "브랜드1");
        ProductJpaEntity product2 = saveProduct("상품2", "브랜드2");
        PricePolicyJpaEntity policy1 = savePricePolicyBasic(product1);
        savePricePolicyBasic(product2);

        List<PricePolicyJpaEntity> results = pricePolicyJpaRepository.findActivePricePoliciesByOffset(
                List.of(policy1.getId()), 0, 10, true, ProductSortProperty.ORDER_NUM, ProductSearchTarget.NAME, null, null
        );

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().getId()).isEqualTo(policy1.getId());
    }

    @Test
    @DisplayName("pricePolicyIds가 null이면 모든 가격 정책이 조회된다")
    void noFilterWhenPricePolicyIdsNull() {
        ProductJpaEntity product1 = saveProduct("상품1", "브랜드1");
        ProductJpaEntity product2 = saveProduct("상품2", "브랜드2");
        savePricePolicyBasic(product1);
        savePricePolicyBasic(product2);

        List<PricePolicyJpaEntity> results = pricePolicyJpaRepository.findActivePricePoliciesByOffset(
                null, 0, 10, true, ProductSortProperty.ORDER_NUM, ProductSearchTarget.NAME, null, null
        );

        assertThat(results).hasSize(2);
    }

    @Test
    @DisplayName("categoryId 지정 시 해당 카테고리 소속 상품만 조회된다")
    void filterByCategoryId() {
        ProductJpaEntity product1 = saveProduct("상품1", "브랜드1");
        ProductJpaEntity product2 = saveProduct("상품2", "브랜드2");
        PricePolicyJpaEntity policy1 = savePricePolicyBasic(product1);
        savePricePolicyBasic(product2);
        Long categoryId = 100L;
        productCategoryJpaRepository.save(ProductCategoryJpaEntity.of(product1.getId(), categoryId));
        entityManager.flush();

        List<PricePolicyJpaEntity> results = pricePolicyJpaRepository.findActivePricePoliciesByOffset(
                null, 0, 10, true, ProductSortProperty.ORDER_NUM, ProductSearchTarget.NAME, null, categoryId
        );

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().getId()).isEqualTo(policy1.getId());
    }

    @Test
    @DisplayName("searchTarget=NAME, searchKeyword 지정 시 상품명으로 LIKE 필터링된다")
    void filterByNameSearch() {
        ProductJpaEntity product1 = saveProduct("노트북-삼성", "brandA");
        ProductJpaEntity product2 = saveProduct("냉장고-LG", "brandB");
        PricePolicyJpaEntity policy1 = savePricePolicyBasic(product1);
        savePricePolicyBasic(product2);

        List<PricePolicyJpaEntity> results = pricePolicyJpaRepository.findActivePricePoliciesByOffset(
                null, 0, 10, true, ProductSortProperty.ORDER_NUM, ProductSearchTarget.NAME, "노트북", null
        );

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().getId()).isEqualTo(policy1.getId());
    }

    @Test
    @DisplayName("searchTarget=BRAND_NAME, searchKeyword 지정 시 브랜드명으로 LIKE 필터링된다")
    void filterByBrandNameSearch() {
        ProductJpaEntity product1 = saveProduct("상품1", "삼성전자");
        ProductJpaEntity product2 = saveProduct("상품2", "LG전자");
        savePricePolicyBasic(product1);
        PricePolicyJpaEntity policy2 = savePricePolicyBasic(product2);

        List<PricePolicyJpaEntity> results = pricePolicyJpaRepository.findActivePricePoliciesByOffset(
                null, 0, 10, true, ProductSortProperty.ORDER_NUM, ProductSearchTarget.BRAND_NAME, "LG", null
        );

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().getId()).isEqualTo(policy2.getId());
    }

    @Test
    @DisplayName("page/pageSize에 따라 offset 페이징된다")
    void offsetPagination() {
        for (int i = 1; i <= 5; i++) {
            ProductJpaEntity product = saveProduct("상품" + i, "브랜드" + i);
            savePricePolicyBasic(product);
        }

        List<PricePolicyJpaEntity> firstPage = pricePolicyJpaRepository.findActivePricePoliciesByOffset(
                null, 0, 2, true, ProductSortProperty.ORDER_NUM, ProductSearchTarget.NAME, null, null
        );
        List<PricePolicyJpaEntity> secondPage = pricePolicyJpaRepository.findActivePricePoliciesByOffset(
                null, 1, 2, true, ProductSortProperty.ORDER_NUM, ProductSearchTarget.NAME, null, null
        );
        List<PricePolicyJpaEntity> thirdPage = pricePolicyJpaRepository.findActivePricePoliciesByOffset(
                null, 2, 2, true, ProductSortProperty.ORDER_NUM, ProductSearchTarget.NAME, null, null
        );

        assertThat(firstPage).hasSize(2);
        assertThat(secondPage).hasSize(2);
        assertThat(thirdPage).hasSize(1);
        assertThat(firstPage.get(0).getId()).isNotEqualTo(secondPage.get(0).getId());
    }

    @Test
    @DisplayName("INACTIVE 상태 가격 정책은 제외된다")
    void excludeInactivePricePolicies() {
        ProductJpaEntity product1 = saveProduct("상품1", "브랜드1");
        ProductJpaEntity product2 = saveProduct("상품2", "브랜드2");
        PricePolicyJpaEntity activePolicy = savePricePolicyBasic(product1);
        PricePolicyJpaEntity inactivePolicy = savePricePolicyBasic(product2);
        inactivePolicy.deactivate();
        entityManager.flush();

        List<PricePolicyJpaEntity> results = pricePolicyJpaRepository.findActivePricePoliciesByOffset(
                null, 0, 10, true, ProductSortProperty.ORDER_NUM, ProductSearchTarget.NAME, null, null
        );

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().getId()).isEqualTo(activePolicy.getId());
    }

    @Test
    @DisplayName("INACTIVE 상태 상품의 가격 정책은 제외된다")
    void excludeInactiveProducts() {
        ProductJpaEntity activeProduct = saveProduct("활성상품", "브랜드1");
        ProductJpaEntity inactiveProduct = saveProduct("비활성상품", "브랜드2");
        PricePolicyJpaEntity activePolicy = savePricePolicyBasic(activeProduct);
        savePricePolicyBasic(inactiveProduct);
        entityManager.createNativeQuery("UPDATE product SET status = 'INACTIVE' WHERE id = :id")
                .setParameter("id", inactiveProduct.getId())
                .executeUpdate();
        entityManager.flush();
        entityManager.clear();

        List<PricePolicyJpaEntity> results = pricePolicyJpaRepository.findActivePricePoliciesByOffset(
                null, 0, 10, true, ProductSortProperty.ORDER_NUM, ProductSearchTarget.NAME, null, null
        );

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().getId()).isEqualTo(activePolicy.getId());
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

    private PricePolicyJpaEntity savePricePolicyBasic(ProductJpaEntity productEntity) {
        return savePricePolicyInternal(productEntity, 9000L, 100L, BigDecimal.valueOf(1.0));
    }

    private PricePolicyJpaEntity savePricePolicyWithDiscountPrice(ProductJpaEntity productEntity, Long discountPrice) {
        return savePricePolicyInternal(productEntity, discountPrice, 100L, BigDecimal.valueOf(1.0));
    }

    private PricePolicyJpaEntity savePricePolicyWithAccumulatedPoint(ProductJpaEntity productEntity, Long accumulatedPoint) {
        return savePricePolicyInternal(productEntity, 9000L, accumulatedPoint, BigDecimal.valueOf(1.0));
    }

    private PricePolicyJpaEntity savePricePolicyWithAccumulationRate(ProductJpaEntity productEntity, BigDecimal rate) {
        return savePricePolicyInternal(productEntity, 9000L, 100L, rate);
    }

    private PricePolicyJpaEntity savePricePolicyWithPopularity(ProductJpaEntity productEntity, Long popularity) {
        PricePolicyJpaEntity saved = savePricePolicyInternal(productEntity, 9000L, 100L, BigDecimal.valueOf(1.0));
        entityManager.createNativeQuery("UPDATE price_policy SET popularity = :pop WHERE id = :id")
                .setParameter("pop", popularity)
                .setParameter("id", saved.getId())
                .executeUpdate();
        entityManager.flush();
        entityManager.clear();
        return pricePolicyJpaRepository.findById(saved.getId()).orElseThrow();
    }

    private PricePolicyJpaEntity savePricePolicyInternal(
            ProductJpaEntity productEntity,
            Long discountPrice,
            Long accumulatedPoint,
            BigDecimal accumulationRate
    ) {
        PricePolicy pricePolicy = PricePolicy.from(
                PricePolicyCreateState.builder()
                        .price(10000L)
                        .discountPrice(discountPrice)
                        .discountRate(Rate.of(BigDecimal.valueOf(10.0)))
                        .accumulatedPoint(accumulatedPoint)
                        .accumulationRate(Rate.of(accumulationRate))
                        .build()
        );
        PricePolicyJpaEntity entity = PricePolicyJpaEntity.from(productEntity, pricePolicy);
        PricePolicyJpaEntity saved = pricePolicyJpaRepository.save(entity);
        saved.setIdToOrderNum();
        return saved;
    }
}
