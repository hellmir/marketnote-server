package com.personal.marketnote.product.adapter.out.persistence.pricepolicy.repository;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.common.configuration.AuditConfig;
import com.personal.marketnote.product.adapter.out.persistence.pricepolicy.entity.PricePolicyJpaEntity;
import com.personal.marketnote.product.adapter.out.persistence.product.entity.ProductJpaEntity;
import com.personal.marketnote.product.adapter.out.persistence.product.repository.ProductJpaRepository;
import com.personal.marketnote.product.adapter.out.persistence.productcategory.entity.ProductCategoryJpaEntity;
import com.personal.marketnote.product.adapter.out.persistence.productcategory.repository.ProductCategoryJpaRepository;
import com.personal.marketnote.product.domain.pricepolicy.PricePolicy;
import com.personal.marketnote.product.domain.pricepolicy.PricePolicyCreateState;
import com.personal.marketnote.product.domain.product.Product;
import com.personal.marketnote.product.domain.product.ProductSnapshotState;
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

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(AuditConfig.class)
class PricePolicySearchQueryTest {

    @Autowired
    private PricePolicyJpaRepository pricePolicyJpaRepository;

    @Autowired
    private ProductJpaRepository productJpaRepository;

    @Autowired
    private ProductCategoryJpaRepository productCategoryJpaRepository;

    private ProductJpaEntity productA;
    private ProductJpaEntity productB;
    private ProductJpaEntity productC;

    private PricePolicyJpaEntity policyA;
    private PricePolicyJpaEntity policyB;
    private PricePolicyJpaEntity policyC;

    @BeforeEach
    void setUp() {
        productA = saveProduct("QA 테스트 상품", "글로벌브랜드");
        productB = saveProduct("일반 상품", "로컬브랜드");
        productC = saveProduct("QA 검증 제품", "글로벌마켓");

        policyA = savePricePolicy(productA);
        policyB = savePricePolicy(productB);
        policyC = savePricePolicy(productC);
    }

    @Nested
    @DisplayName("상품명 검색 (searchTarget=name)")
    class SearchByName {

        @Test
        @DisplayName("상품명에 QA가 포함된 상품만 조회된다 (DESC)")
        void filtersCorrectly_desc() {
            Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "orderNum"));

            List<PricePolicyJpaEntity> results = pricePolicyJpaRepository.findAllActiveByCursorDesc(
                    null, null, pageable, "orderNum", "name", "%QA%", null
            );

            assertThat(results).hasSize(2);
            assertThat(results)
                    .allMatch(pp -> pp.getProductJpaEntity().getName().contains("QA"));
        }

        @Test
        @DisplayName("상품명에 QA가 포함된 상품만 조회된다 (ASC)")
        void filtersCorrectly_asc() {
            Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "orderNum"));

            List<PricePolicyJpaEntity> results = pricePolicyJpaRepository.findAllActiveByCursorAsc(
                    null, null, pageable, "orderNum", "name", "%QA%", null
            );

            assertThat(results).hasSize(2);
            assertThat(results)
                    .allMatch(pp -> pp.getProductJpaEntity().getName().contains("QA"));
        }

        @Test
        @DisplayName("대소문자를 구분하여 검색한다")
        void caseSensitiveSearch() {
            Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "orderNum"));

            List<PricePolicyJpaEntity> lowerCaseResults = pricePolicyJpaRepository.findAllActiveByCursorDesc(
                    null, null, pageable, "orderNum", "name", "%qa%", null
            );

            assertThat(lowerCaseResults).isEmpty();
        }
    }

    @Nested
    @DisplayName("브랜드명 검색 (searchTarget=brandName)")
    class SearchByBrandName {

        @Test
        @DisplayName("브랜드명에 글로가 포함된 상품만 조회된다")
        void filtersCorrectly() {
            Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "orderNum"));

            List<PricePolicyJpaEntity> results = pricePolicyJpaRepository.findAllActiveByCursorDesc(
                    null, null, pageable, "orderNum", "brandName", "%글로%", null
            );

            assertThat(results).hasSize(2);
            assertThat(results)
                    .allMatch(pp -> pp.getProductJpaEntity().getBrandName().contains("글로"));
        }
    }

    @Nested
    @DisplayName("검색 조건 없음 (빈 pattern)")
    class NoSearchCondition {

        @Test
        @DisplayName("pattern이 빈 문자열이면 모든 활성 상품이 조회된다")
        void returnsAll() {
            Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "orderNum"));

            List<PricePolicyJpaEntity> results = pricePolicyJpaRepository.findAllActiveByCursorDesc(
                    null, null, pageable, "orderNum", "name", "", null
            );

            assertThat(results).hasSize(3);
        }
    }

    @Nested
    @DisplayName("매칭 불가 검색")
    class NonMatchingSearch {

        @Test
        @DisplayName("매칭되지 않는 키워드면 빈 결과를 반환한다")
        void returnsEmpty() {
            Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "orderNum"));

            List<PricePolicyJpaEntity> results = pricePolicyJpaRepository.findAllActiveByCursorDesc(
                    null, null, pageable, "orderNum", "name", "%존재하지않는키워드%", null
            );

            assertThat(results).isEmpty();
        }
    }

    @Nested
    @DisplayName("카테고리 + 검색 조합")
    class CategoryWithSearch {

        @Test
        @DisplayName("카테고리와 검색 조건을 동시에 적용한다")
        void filtersByCategoryAndSearch() {
            Long categoryId = 100L;
            productCategoryJpaRepository.save(ProductCategoryJpaEntity.of(productA.getId(), categoryId));
            productCategoryJpaRepository.save(ProductCategoryJpaEntity.of(productC.getId(), categoryId));

            Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "orderNum"));

            List<PricePolicyJpaEntity> allInCategory = pricePolicyJpaRepository.findAllActiveByCursorDesc(
                    null, null, pageable, "orderNum", "name", "", categoryId
            );
            assertThat(allInCategory).hasSize(2);

            List<PricePolicyJpaEntity> searchInCategory = pricePolicyJpaRepository.findAllActiveByCursorDesc(
                    null, null, pageable, "orderNum", "name", "%테스트%", categoryId
            );
            assertThat(searchInCategory).hasSize(1);
            assertThat(searchInCategory.getFirst().getProductJpaEntity().getName()).contains("테스트");
        }

        @Test
        @DisplayName("countActiveByCategoryId도 검색 조건을 적용한다")
        void countByCategoryAndSearch() {
            Long categoryId = 200L;
            productCategoryJpaRepository.save(ProductCategoryJpaEntity.of(productA.getId(), categoryId));
            productCategoryJpaRepository.save(ProductCategoryJpaEntity.of(productB.getId(), categoryId));
            productCategoryJpaRepository.save(ProductCategoryJpaEntity.of(productC.getId(), categoryId));

            long totalInCategory = pricePolicyJpaRepository.countActiveByCategoryId(categoryId, "name", "");
            long filteredInCategory = pricePolicyJpaRepository.countActiveByCategoryId(categoryId, "name", "%QA%");

            assertThat(totalInCategory).isEqualTo(3);
            assertThat(filteredInCategory).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("전체 개수 검색 (countActive)")
    class CountActive {

        @Test
        @DisplayName("검색 조건을 올바르게 적용한다")
        void filtersCorrectly() {
            long totalCount = pricePolicyJpaRepository.countActive("name", "");
            long nameFiltered = pricePolicyJpaRepository.countActive("name", "%QA%");
            long brandFiltered = pricePolicyJpaRepository.countActive("brandName", "%글로%");

            assertThat(totalCount).isEqualTo(3);
            assertThat(nameFiltered).isEqualTo(2);
            assertThat(brandFiltered).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("적립률 정렬 (sortProperty=accumulatedPointRate)")
    class SortByAccumulatedPointRate {

        private PricePolicyJpaEntity policyRate5First;
        private PricePolicyJpaEntity policyRate3First;
        private PricePolicyJpaEntity policyRate3Second;
        private PricePolicyJpaEntity policyRate1;
        private PricePolicyJpaEntity policyRate5Second;

        @BeforeEach
        void setUp() {
            pricePolicyJpaRepository.deleteAll();

            ProductJpaEntity product1 = saveProduct("상품A", "브랜드A");
            ProductJpaEntity product2 = saveProduct("상품B", "브랜드B");
            ProductJpaEntity product3 = saveProduct("상품C", "브랜드C");
            ProductJpaEntity product4 = saveProduct("상품D", "브랜드D");
            ProductJpaEntity product5 = saveProduct("상품E", "브랜드E");

            policyRate5First = savePricePolicy(product1, BigDecimal.valueOf(5.0));
            policyRate3First = savePricePolicy(product2, BigDecimal.valueOf(3.0));
            policyRate3Second = savePricePolicy(product3, BigDecimal.valueOf(3.0));
            policyRate1 = savePricePolicy(product4, BigDecimal.valueOf(1.0));
            policyRate5Second = savePricePolicy(product5, BigDecimal.valueOf(5.0));
        }

        @Test
        @DisplayName("적립률 오름차순 정렬 시 낮은 적립률부터 조회된다")
        void sortsAscending() {
            Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "accumulationRate"));

            List<PricePolicyJpaEntity> results = pricePolicyJpaRepository.findAllActiveByCursorAsc(
                    null, null, pageable, "accumulatedPointRate", "name", "", null
            );

            assertThat(results).hasSize(5);
            assertThat(results.get(0).getAccumulationRate()).isEqualByComparingTo(BigDecimal.valueOf(1.0));
            assertThat(results.get(1).getAccumulationRate()).isEqualByComparingTo(BigDecimal.valueOf(3.0));
            assertThat(results.get(2).getAccumulationRate()).isEqualByComparingTo(BigDecimal.valueOf(3.0));
            assertThat(results.get(3).getAccumulationRate()).isEqualByComparingTo(BigDecimal.valueOf(5.0));
            assertThat(results.get(4).getAccumulationRate()).isEqualByComparingTo(BigDecimal.valueOf(5.0));
        }

        @Test
        @DisplayName("적립률 내림차순 정렬 시 높은 적립률부터 조회된다")
        void sortsDescending() {
            Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "accumulationRate"));

            List<PricePolicyJpaEntity> results = pricePolicyJpaRepository.findAllActiveByCursorDesc(
                    null, null, pageable, "accumulatedPointRate", "name", "", null
            );

            assertThat(results).hasSize(5);
            assertThat(results.get(0).getAccumulationRate()).isEqualByComparingTo(BigDecimal.valueOf(5.0));
            assertThat(results.get(1).getAccumulationRate()).isEqualByComparingTo(BigDecimal.valueOf(5.0));
            assertThat(results.get(2).getAccumulationRate()).isEqualByComparingTo(BigDecimal.valueOf(3.0));
            assertThat(results.get(3).getAccumulationRate()).isEqualByComparingTo(BigDecimal.valueOf(3.0));
            assertThat(results.get(4).getAccumulationRate()).isEqualByComparingTo(BigDecimal.valueOf(1.0));
        }

        @Test
        @DisplayName("적립률 내림차순 커서 기반 페이징 시 커서 이후 데이터만 조회된다")
        void cursorPaginationDesc() {
            Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "accumulationRate"));

            List<PricePolicyJpaEntity> results = pricePolicyJpaRepository.findAllActiveByCursorDesc(
                    null, policyRate5Second.getId(), pageable, "accumulatedPointRate", "name", "", null
            );

            assertThat(results).hasSize(4);
            assertThat(results.get(0).getId()).isEqualTo(policyRate5First.getId());
            assertThat(results.get(1).getId()).isEqualTo(policyRate3Second.getId());
            assertThat(results.get(2).getId()).isEqualTo(policyRate3First.getId());
            assertThat(results.get(3).getId()).isEqualTo(policyRate1.getId());
        }

        @Test
        @DisplayName("적립률 오름차순 커서 기반 페이징 시 커서 이후 데이터만 조회된다")
        void cursorPaginationAsc() {
            Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "accumulationRate"));

            List<PricePolicyJpaEntity> results = pricePolicyJpaRepository.findAllActiveByCursorAsc(
                    null, policyRate1.getId(), pageable, "accumulatedPointRate", "name", "", null
            );

            assertThat(results).hasSize(4);
            assertThat(results.get(0).getId()).isEqualTo(policyRate3Second.getId());
            assertThat(results.get(1).getId()).isEqualTo(policyRate3First.getId());
            assertThat(results.get(2).getId()).isEqualTo(policyRate5Second.getId());
            assertThat(results.get(3).getId()).isEqualTo(policyRate5First.getId());
        }

        @Test
        @DisplayName("전체 정렬 순서를 검증한다: accumulationRate DESC → p.id DESC")
        void fullSortOrderDesc() {
            Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "accumulationRate"));

            List<PricePolicyJpaEntity> results = pricePolicyJpaRepository.findAllActiveByCursorDesc(
                    null, null, pageable, "accumulatedPointRate", "name", "", null
            );

            assertThat(results).extracting(PricePolicyJpaEntity::getId)
                    .containsExactly(
                            policyRate5Second.getId(),
                            policyRate5First.getId(),
                            policyRate3Second.getId(),
                            policyRate3First.getId(),
                            policyRate1.getId()
                    );
        }

        @Test
        @DisplayName("전체 정렬 순서를 검증한다: accumulationRate ASC → p.id DESC")
        void fullSortOrderAsc() {
            Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "accumulationRate"));

            List<PricePolicyJpaEntity> results = pricePolicyJpaRepository.findAllActiveByCursorAsc(
                    null, null, pageable, "accumulatedPointRate", "name", "", null
            );

            assertThat(results).extracting(PricePolicyJpaEntity::getId)
                    .containsExactly(
                            policyRate1.getId(),
                            policyRate3Second.getId(),
                            policyRate3First.getId(),
                            policyRate5Second.getId(),
                            policyRate5First.getId()
                    );
        }

        @Test
        @DisplayName("적립률 내림차순 커서 기반 페이징이 동일 적립률 상품을 올바르게 분할한다")
        void cursorPaginationSplitsSameRateDesc() {
            Pageable pageable = PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "accumulationRate"));

            List<PricePolicyJpaEntity> page1 = pricePolicyJpaRepository.findAllActiveByCursorDesc(
                    null, null, pageable, "accumulatedPointRate", "name", "", null
            );
            assertThat(page1).extracting(PricePolicyJpaEntity::getId)
                    .containsExactly(policyRate5Second.getId(), policyRate5First.getId());

            List<PricePolicyJpaEntity> page2 = pricePolicyJpaRepository.findAllActiveByCursorDesc(
                    null, page1.getLast().getId(), pageable, "accumulatedPointRate", "name", "", null
            );
            assertThat(page2).extracting(PricePolicyJpaEntity::getId)
                    .containsExactly(policyRate3Second.getId(), policyRate3First.getId());

            List<PricePolicyJpaEntity> page3 = pricePolicyJpaRepository.findAllActiveByCursorDesc(
                    null, page2.getLast().getId(), pageable, "accumulatedPointRate", "name", "", null
            );
            assertThat(page3).extracting(PricePolicyJpaEntity::getId)
                    .containsExactly(policyRate1.getId());
        }

        @Test
        @DisplayName("적립률 오름차순 커서 기반 페이징이 동일 적립률 상품을 올바르게 분할한다")
        void cursorPaginationSplitsSameRateAsc() {
            Pageable pageable = PageRequest.of(0, 2, Sort.by(Sort.Direction.ASC, "accumulationRate"));

            List<PricePolicyJpaEntity> page1 = pricePolicyJpaRepository.findAllActiveByCursorAsc(
                    null, null, pageable, "accumulatedPointRate", "name", "", null
            );
            assertThat(page1).extracting(PricePolicyJpaEntity::getId)
                    .containsExactly(policyRate1.getId(), policyRate3Second.getId());

            List<PricePolicyJpaEntity> page2 = pricePolicyJpaRepository.findAllActiveByCursorAsc(
                    null, page1.getLast().getId(), pageable, "accumulatedPointRate", "name", "", null
            );
            assertThat(page2).extracting(PricePolicyJpaEntity::getId)
                    .containsExactly(policyRate3First.getId(), policyRate5Second.getId());

            List<PricePolicyJpaEntity> page3 = pricePolicyJpaRepository.findAllActiveByCursorAsc(
                    null, page2.getLast().getId(), pageable, "accumulatedPointRate", "name", "", null
            );
            assertThat(page3).extracting(PricePolicyJpaEntity::getId)
                    .containsExactly(policyRate5First.getId());
        }
    }

    @Nested
    @DisplayName("적립금 정렬 (sortProperty=accumulatedPoint)")
    class SortByAccumulatedPoint {

        private PricePolicyJpaEntity policyPoint1000First;
        private PricePolicyJpaEntity policyPoint500First;
        private PricePolicyJpaEntity policyPoint500Second;
        private PricePolicyJpaEntity policyPoint2000;
        private PricePolicyJpaEntity policyPoint1000Second;

        @BeforeEach
        void setUp() {
            pricePolicyJpaRepository.deleteAll();

            ProductJpaEntity product1 = saveProduct("상품A", "브랜드A");
            ProductJpaEntity product2 = saveProduct("상품B", "브랜드B");
            ProductJpaEntity product3 = saveProduct("상품C", "브랜드C");
            ProductJpaEntity product4 = saveProduct("상품D", "브랜드D");
            ProductJpaEntity product5 = saveProduct("상품E", "브랜드E");

            policyPoint1000First = savePricePolicy(product1, 1000L);
            policyPoint500First = savePricePolicy(product2, 500L);
            policyPoint500Second = savePricePolicy(product3, 500L);
            policyPoint2000 = savePricePolicy(product4, 2000L);
            policyPoint1000Second = savePricePolicy(product5, 1000L);
        }

        @Test
        @DisplayName("적립금 오름차순 정렬 시 낮은 적립금부터 조회된다")
        void sortsAscending() {
            Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "accumulatedPoint"));

            List<PricePolicyJpaEntity> results = pricePolicyJpaRepository.findAllActiveByCursorAsc(
                    null, null, pageable, "accumulatedPoint", "name", "", null
            );

            assertThat(results).hasSize(5);
            assertThat(results.get(0).getAccumulatedPoint()).isEqualTo(500L);
            assertThat(results.get(1).getAccumulatedPoint()).isEqualTo(500L);
            assertThat(results.get(2).getAccumulatedPoint()).isEqualTo(1000L);
            assertThat(results.get(3).getAccumulatedPoint()).isEqualTo(1000L);
            assertThat(results.get(4).getAccumulatedPoint()).isEqualTo(2000L);
        }

        @Test
        @DisplayName("적립금 내림차순 정렬 시 높은 적립금부터 조회된다")
        void sortsDescending() {
            Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "accumulatedPoint"));

            List<PricePolicyJpaEntity> results = pricePolicyJpaRepository.findAllActiveByCursorDesc(
                    null, null, pageable, "accumulatedPoint", "name", "", null
            );

            assertThat(results).hasSize(5);
            assertThat(results.get(0).getAccumulatedPoint()).isEqualTo(2000L);
            assertThat(results.get(1).getAccumulatedPoint()).isEqualTo(1000L);
            assertThat(results.get(2).getAccumulatedPoint()).isEqualTo(1000L);
            assertThat(results.get(3).getAccumulatedPoint()).isEqualTo(500L);
            assertThat(results.get(4).getAccumulatedPoint()).isEqualTo(500L);
        }

        @Test
        @DisplayName("적립금 내림차순 커서 기반 페이징 시 커서 이후 데이터만 조회된다")
        void cursorPaginationDesc() {
            Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "accumulatedPoint"));

            List<PricePolicyJpaEntity> results = pricePolicyJpaRepository.findAllActiveByCursorDesc(
                    null, policyPoint2000.getId(), pageable, "accumulatedPoint", "name", "", null
            );

            assertThat(results).hasSize(4);
            assertThat(results.get(0).getId()).isEqualTo(policyPoint1000Second.getId());
            assertThat(results.get(1).getId()).isEqualTo(policyPoint1000First.getId());
            assertThat(results.get(2).getId()).isEqualTo(policyPoint500Second.getId());
            assertThat(results.get(3).getId()).isEqualTo(policyPoint500First.getId());
        }

        @Test
        @DisplayName("적립금 오름차순 커서 기반 페이징 시 커서 이후 데이터만 조회된다")
        void cursorPaginationAsc() {
            Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "accumulatedPoint"));

            List<PricePolicyJpaEntity> results = pricePolicyJpaRepository.findAllActiveByCursorAsc(
                    null, policyPoint500Second.getId(), pageable, "accumulatedPoint", "name", "", null
            );

            assertThat(results).hasSize(4);
            assertThat(results.get(0).getId()).isEqualTo(policyPoint500First.getId());
            assertThat(results.get(1).getId()).isEqualTo(policyPoint1000Second.getId());
            assertThat(results.get(2).getId()).isEqualTo(policyPoint1000First.getId());
            assertThat(results.get(3).getId()).isEqualTo(policyPoint2000.getId());
        }

        @Test
        @DisplayName("전체 정렬 순서를 검증한다: accumulatedPoint DESC → p.id DESC")
        void fullSortOrderDesc() {
            Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "accumulatedPoint"));

            List<PricePolicyJpaEntity> results = pricePolicyJpaRepository.findAllActiveByCursorDesc(
                    null, null, pageable, "accumulatedPoint", "name", "", null
            );

            assertThat(results).extracting(PricePolicyJpaEntity::getId)
                    .containsExactly(
                            policyPoint2000.getId(),
                            policyPoint1000Second.getId(),
                            policyPoint1000First.getId(),
                            policyPoint500Second.getId(),
                            policyPoint500First.getId()
                    );
        }

        @Test
        @DisplayName("전체 정렬 순서를 검증한다: accumulatedPoint ASC → p.id DESC")
        void fullSortOrderAsc() {
            Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "accumulatedPoint"));

            List<PricePolicyJpaEntity> results = pricePolicyJpaRepository.findAllActiveByCursorAsc(
                    null, null, pageable, "accumulatedPoint", "name", "", null
            );

            assertThat(results).extracting(PricePolicyJpaEntity::getId)
                    .containsExactly(
                            policyPoint500Second.getId(),
                            policyPoint500First.getId(),
                            policyPoint1000Second.getId(),
                            policyPoint1000First.getId(),
                            policyPoint2000.getId()
                    );
        }

        @Test
        @DisplayName("적립금 내림차순 커서 기반 페이징이 동일 적립금 상품을 올바르게 분할한다")
        void cursorPaginationSplitsSamePointDesc() {
            Pageable pageable = PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "accumulatedPoint"));

            List<PricePolicyJpaEntity> page1 = pricePolicyJpaRepository.findAllActiveByCursorDesc(
                    null, null, pageable, "accumulatedPoint", "name", "", null
            );
            assertThat(page1).extracting(PricePolicyJpaEntity::getId)
                    .containsExactly(policyPoint2000.getId(), policyPoint1000Second.getId());

            List<PricePolicyJpaEntity> page2 = pricePolicyJpaRepository.findAllActiveByCursorDesc(
                    null, page1.getLast().getId(), pageable, "accumulatedPoint", "name", "", null
            );
            assertThat(page2).extracting(PricePolicyJpaEntity::getId)
                    .containsExactly(policyPoint1000First.getId(), policyPoint500Second.getId());

            List<PricePolicyJpaEntity> page3 = pricePolicyJpaRepository.findAllActiveByCursorDesc(
                    null, page2.getLast().getId(), pageable, "accumulatedPoint", "name", "", null
            );
            assertThat(page3).extracting(PricePolicyJpaEntity::getId)
                    .containsExactly(policyPoint500First.getId());
        }

        @Test
        @DisplayName("적립금 오름차순 커서 기반 페이징이 동일 적립금 상품을 올바르게 분할한다")
        void cursorPaginationSplitsSamePointAsc() {
            Pageable pageable = PageRequest.of(0, 2, Sort.by(Sort.Direction.ASC, "accumulatedPoint"));

            List<PricePolicyJpaEntity> page1 = pricePolicyJpaRepository.findAllActiveByCursorAsc(
                    null, null, pageable, "accumulatedPoint", "name", "", null
            );
            assertThat(page1).extracting(PricePolicyJpaEntity::getId)
                    .containsExactly(policyPoint500Second.getId(), policyPoint500First.getId());

            List<PricePolicyJpaEntity> page2 = pricePolicyJpaRepository.findAllActiveByCursorAsc(
                    null, page1.getLast().getId(), pageable, "accumulatedPoint", "name", "", null
            );
            assertThat(page2).extracting(PricePolicyJpaEntity::getId)
                    .containsExactly(policyPoint1000Second.getId(), policyPoint1000First.getId());

            List<PricePolicyJpaEntity> page3 = pricePolicyJpaRepository.findAllActiveByCursorAsc(
                    null, page2.getLast().getId(), pageable, "accumulatedPoint", "name", "", null
            );
            assertThat(page3).extracting(PricePolicyJpaEntity::getId)
                    .containsExactly(policyPoint2000.getId());
        }
    }

    @Nested
    @DisplayName("pricePolicyIds 필터링")
    class PricePolicyIdsFilter {

        @Test
        @DisplayName("pricePolicyIds 지정 시 해당 ID만 조회된다")
        void filtersByIds() {
            Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "orderNum"));

            List<PricePolicyJpaEntity> results = pricePolicyJpaRepository.findAllActiveByCursorDesc(
                    List.of(policyA.getId()), null, pageable, "orderNum", "name", "", null
            );

            assertThat(results).hasSize(1);
            assertThat(results.getFirst().getId()).isEqualTo(policyA.getId());
        }

        @Test
        @DisplayName("pricePolicyIds와 검색 조건을 동시에 적용한다")
        void filtersByIdsAndSearch() {
            Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "orderNum"));

            List<PricePolicyJpaEntity> results = pricePolicyJpaRepository.findAllActiveByCursorDesc(
                    List.of(policyA.getId(), policyB.getId()), null, pageable, "orderNum", "name", "%QA%", null
            );

            assertThat(results).hasSize(1);
            assertThat(results.getFirst().getProductJpaEntity().getName()).contains("QA");
        }
    }

    private ProductJpaEntity saveProduct(String name, String brandName) {
        Product product = Product.from(
                ProductSnapshotState.builder()
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
        return savePricePolicy(productEntity, BigDecimal.valueOf(1.0));
    }

    private PricePolicyJpaEntity savePricePolicy(ProductJpaEntity productEntity, Long accumulatedPoint) {
        PricePolicy pricePolicy = PricePolicy.from(
                PricePolicyCreateState.builder()
                        .price(10000L)
                        .discountPrice(9000L)
                        .discountRate(BigDecimal.valueOf(10.0))
                        .accumulatedPoint(accumulatedPoint)
                        .accumulationRate(BigDecimal.valueOf(1.0))
                        .build()
        );
        PricePolicyJpaEntity entity = PricePolicyJpaEntity.from(productEntity, pricePolicy);
        PricePolicyJpaEntity saved = pricePolicyJpaRepository.save(entity);
        saved.setIdToOrderNum();
        return saved;
    }

    private PricePolicyJpaEntity savePricePolicy(ProductJpaEntity productEntity, BigDecimal accumulationRate) {
        PricePolicy pricePolicy = PricePolicy.from(
                PricePolicyCreateState.builder()
                        .price(10000L)
                        .discountPrice(9000L)
                        .discountRate(BigDecimal.valueOf(10.0))
                        .accumulatedPoint(100L)
                        .accumulationRate(accumulationRate)
                        .build()
        );
        PricePolicyJpaEntity entity = PricePolicyJpaEntity.from(productEntity, pricePolicy);
        PricePolicyJpaEntity saved = pricePolicyJpaRepository.save(entity);
        saved.setIdToOrderNum();
        return saved;
    }
}
