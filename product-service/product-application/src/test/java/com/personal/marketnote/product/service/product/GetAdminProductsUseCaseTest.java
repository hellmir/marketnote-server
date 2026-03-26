package com.personal.marketnote.product.service.product;

import com.personal.marketnote.common.adapter.out.persistence.audit.EntityStatus;
import com.personal.marketnote.product.domain.pricepolicy.PricePolicy;
import com.personal.marketnote.product.domain.pricepolicy.PricePolicySnapshotState;
import com.personal.marketnote.product.domain.product.Product;
import com.personal.marketnote.product.domain.product.ProductSearchTarget;
import com.personal.marketnote.product.domain.product.ProductSnapshotState;
import com.personal.marketnote.product.domain.product.ProductSortProperty;
import com.personal.marketnote.product.port.in.result.product.GetAdminProductsResult;
import com.personal.marketnote.product.port.in.usecase.product.GetProductInventoryUseCase;
import com.personal.marketnote.product.port.out.file.FindProductImagesPort;
import com.personal.marketnote.product.port.out.pricepolicy.FindPricePoliciesPort;
import com.personal.marketnote.product.port.out.product.FindProductPort;
import com.personal.marketnote.product.port.out.productoption.FindProductOptionCategoryPort;
import com.personal.marketnote.product.port.out.review.FindProductReviewAggregatesPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetAdminProductsUseCaseTest {
    @Mock
    private FindPricePoliciesPort findPricePoliciesPort;

    @Mock
    private FindProductPort findProductPort;

    @Mock
    private FindProductImagesPort findProductImagesPort;

    @Mock
    private FindProductReviewAggregatesPort findProductReviewAggregatesPort;

    @Mock
    private GetProductInventoryUseCase getProductInventoryUseCase;

    @Mock
    private FindProductOptionCategoryPort findProductOptionCategoryPort;

    @Mock
    private Executor productImageExecutor;

    @InjectMocks
    private GetAdminProductsService getAdminProductsService;

    @Test
    @DisplayName("관리자 상품 목록 조회 시 오프셋 페이징 결과를 반환한다")
    void getAdminProducts_returnsOffsetPaginatedResult() {
        // given
        int page = 0;
        int pageSize = 10;
        long totalElements = 25L;

        List<PricePolicy> pricePolicies = List.of(
                createPricePolicyWithProduct(1L, 100L),
                createPricePolicyWithProduct(2L, 200L)
        );

        when(findPricePoliciesPort.findPricePoliciesByOffset(
                null, page, pageSize, Sort.Direction.DESC,
                ProductSortProperty.ORDER_NUM, ProductSearchTarget.NAME, null, null
        )).thenReturn(pricePolicies);

        when(findPricePoliciesPort.countActivePricePoliciesByCategoryId(
                null, ProductSearchTarget.NAME, null
        )).thenReturn(totalElements);

        stubEnrichment(100L, 200L);

        // when
        GetAdminProductsResult result = getAdminProductsService.getAdminProducts(
                null, null, page, pageSize,
                Sort.Direction.DESC, ProductSortProperty.ORDER_NUM,
                ProductSearchTarget.NAME, null
        );

        // then
        assertThat(result.page()).isEqualTo(0);
        assertThat(result.pageSize()).isEqualTo(10);
        assertThat(result.totalElements()).isEqualTo(25L);
        assertThat(result.totalPages()).isEqualTo(3);
        assertThat(result.products()).hasSize(2);

        verifyNoInteractions(findProductPort);
    }

    @Test
    @DisplayName("관리자 상품 목록 조회 시 카테고리 필터를 적용한다")
    void getAdminProducts_withCategoryFilter_appliesCategoryId() {
        // given
        Long categoryId = 5L;
        int page = 0;
        int pageSize = 10;

        List<PricePolicy> pricePolicies = List.of(createPricePolicyWithProduct(1L, 100L));

        when(findPricePoliciesPort.findPricePoliciesByOffset(
                null, page, pageSize, Sort.Direction.DESC,
                ProductSortProperty.ORDER_NUM, ProductSearchTarget.NAME, null, categoryId
        )).thenReturn(pricePolicies);

        when(findPricePoliciesPort.countActivePricePoliciesByCategoryId(
                categoryId, ProductSearchTarget.NAME, null
        )).thenReturn(1L);

        stubEnrichment(100L);

        // when
        GetAdminProductsResult result = getAdminProductsService.getAdminProducts(
                categoryId, null, page, pageSize,
                Sort.Direction.DESC, ProductSortProperty.ORDER_NUM,
                ProductSearchTarget.NAME, null
        );

        // then
        assertThat(result.totalElements()).isEqualTo(1L);
        assertThat(result.products()).hasSize(1);

        verify(findPricePoliciesPort).findPricePoliciesByOffset(
                null, page, pageSize, Sort.Direction.DESC,
                ProductSortProperty.ORDER_NUM, ProductSearchTarget.NAME, null, categoryId
        );
    }

    @Test
    @DisplayName("관리자 상품 목록 조회 시 검색 키워드를 적용한다")
    void getAdminProducts_withSearchKeyword_appliesKeyword() {
        // given
        String searchKeyword = "비타민";
        int page = 0;
        int pageSize = 10;

        List<PricePolicy> pricePolicies = List.of(createPricePolicyWithProduct(1L, 100L));

        when(findPricePoliciesPort.findPricePoliciesByOffset(
                null, page, pageSize, Sort.Direction.DESC,
                ProductSortProperty.ORDER_NUM, ProductSearchTarget.NAME, searchKeyword, null
        )).thenReturn(pricePolicies);

        when(findPricePoliciesPort.countActivePricePoliciesByCategoryId(
                null, ProductSearchTarget.NAME, searchKeyword
        )).thenReturn(1L);

        stubEnrichment(100L);

        // when
        GetAdminProductsResult result = getAdminProductsService.getAdminProducts(
                null, null, page, pageSize,
                Sort.Direction.DESC, ProductSortProperty.ORDER_NUM,
                ProductSearchTarget.NAME, searchKeyword
        );

        // then
        assertThat(result.totalElements()).isEqualTo(1L);
        assertThat(result.products()).hasSize(1);
    }

    @Test
    @DisplayName("관리자 상품 목록 조회 시 결과가 없으면 빈 목록을 반환한다")
    void getAdminProducts_noResults_returnsEmptyList() {
        // given
        int page = 0;
        int pageSize = 10;

        when(findPricePoliciesPort.findPricePoliciesByOffset(
                null, page, pageSize, Sort.Direction.DESC,
                ProductSortProperty.ORDER_NUM, ProductSearchTarget.NAME, null, null
        )).thenReturn(List.of());

        when(findPricePoliciesPort.countActivePricePoliciesByCategoryId(
                null, ProductSearchTarget.NAME, null
        )).thenReturn(0L);

        // when
        GetAdminProductsResult result = getAdminProductsService.getAdminProducts(
                null, null, page, pageSize,
                Sort.Direction.DESC, ProductSortProperty.ORDER_NUM,
                ProductSearchTarget.NAME, null
        );

        // then
        assertThat(result.page()).isEqualTo(0);
        assertThat(result.totalElements()).isEqualTo(0L);
        assertThat(result.totalPages()).isEqualTo(0);
        assertThat(result.products()).isEmpty();
    }

    @Test
    @DisplayName("관리자 상품 목록 조회 시 totalPages를 올바르게 계산한다")
    void getAdminProducts_calculatesTotalPagesCorrectly() {
        // given
        int page = 2;
        int pageSize = 10;
        long totalElements = 30L;

        when(findPricePoliciesPort.findPricePoliciesByOffset(
                null, page, pageSize, Sort.Direction.DESC,
                ProductSortProperty.ORDER_NUM, ProductSearchTarget.NAME, null, null
        )).thenReturn(List.of());

        when(findPricePoliciesPort.countActivePricePoliciesByCategoryId(
                null, ProductSearchTarget.NAME, null
        )).thenReturn(totalElements);

        // when
        GetAdminProductsResult result = getAdminProductsService.getAdminProducts(
                null, null, page, pageSize,
                Sort.Direction.DESC, ProductSortProperty.ORDER_NUM,
                ProductSearchTarget.NAME, null
        );

        // then
        assertThat(result.totalPages()).isEqualTo(3);
    }

    @Test
    @DisplayName("관리자 상품 목록 조회 시 totalElements가 pageSize로 나누어떨어지지 않으면 totalPages를 올림한다")
    void getAdminProducts_totalElementsNotDivisible_roundsUpTotalPages() {
        // given
        int page = 0;
        int pageSize = 10;
        long totalElements = 21L;

        List<PricePolicy> pricePolicies = List.of(createPricePolicyWithProduct(1L, 100L));

        when(findPricePoliciesPort.findPricePoliciesByOffset(
                null, page, pageSize, Sort.Direction.DESC,
                ProductSortProperty.ORDER_NUM, ProductSearchTarget.NAME, null, null
        )).thenReturn(pricePolicies);

        when(findPricePoliciesPort.countActivePricePoliciesByCategoryId(
                null, ProductSearchTarget.NAME, null
        )).thenReturn(totalElements);

        stubEnrichment(100L);

        // when
        GetAdminProductsResult result = getAdminProductsService.getAdminProducts(
                null, null, page, pageSize,
                Sort.Direction.DESC, ProductSortProperty.ORDER_NUM,
                ProductSearchTarget.NAME, null
        );

        // then
        assertThat(result.totalPages()).isEqualTo(3);
    }

    @Test
    @DisplayName("관리자 상품 목록 조회 시 재고 수량을 함께 조회한다")
    void getAdminProducts_enrichesWithInventory() {
        // given
        int page = 0;
        int pageSize = 10;

        List<PricePolicy> pricePolicies = List.of(createPricePolicyWithProduct(1L, 100L));

        when(findPricePoliciesPort.findPricePoliciesByOffset(
                null, page, pageSize, Sort.Direction.DESC,
                ProductSortProperty.ORDER_NUM, ProductSearchTarget.NAME, null, null
        )).thenReturn(pricePolicies);

        when(findPricePoliciesPort.countActivePricePoliciesByCategoryId(
                null, ProductSearchTarget.NAME, null
        )).thenReturn(1L);

        when(findProductReviewAggregatesPort.findByProductIds(List.of(100L)))
                .thenReturn(Map.of());
        when(findProductImagesPort.findImagesByProductIdAndSort(eq(100L), any()))
                .thenReturn(Optional.empty());
        when(getProductInventoryUseCase.getProductStocks(List.of(1L)))
                .thenReturn(Map.of(1L, 50));

        stubExecutorToRunDirectly();

        // when
        GetAdminProductsResult result = getAdminProductsService.getAdminProducts(
                null, null, page, pageSize,
                Sort.Direction.DESC, ProductSortProperty.ORDER_NUM,
                ProductSearchTarget.NAME, null
        );

        // then
        assertThat(result.products()).hasSize(1);
        assertThat(result.products().getFirst().getStock()).isEqualTo(50);

        verify(getProductInventoryUseCase).getProductStocks(List.of(1L));
    }

    @Test
    @DisplayName("관리자 상품 목록 조회 시 PricePolicy에 Product가 포함되어 있으면 추가 조회하지 않는다")
    void getAdminProducts_productAlreadyLoaded_doesNotQueryPort() {
        // given
        int page = 0;
        int pageSize = 10;

        List<PricePolicy> pricePolicies = List.of(createPricePolicyWithProduct(1L, 100L));

        when(findPricePoliciesPort.findPricePoliciesByOffset(
                null, page, pageSize, Sort.Direction.DESC,
                ProductSortProperty.ORDER_NUM, ProductSearchTarget.NAME, null, null
        )).thenReturn(pricePolicies);

        when(findPricePoliciesPort.countActivePricePoliciesByCategoryId(
                null, ProductSearchTarget.NAME, null
        )).thenReturn(1L);

        stubEnrichment(100L);

        // when
        getAdminProductsService.getAdminProducts(
                null, null, page, pageSize,
                Sort.Direction.DESC, ProductSortProperty.ORDER_NUM,
                ProductSearchTarget.NAME, null
        );

        // then
        verifyNoInteractions(findProductPort);
    }

    private PricePolicy createPricePolicyWithProduct(Long id, Long productId) {
        Product product = createProduct(productId);
        PricePolicy pricePolicy = PricePolicy.from(
                PricePolicySnapshotState.builder()
                        .id(id)
                        .productId(productId)
                        .price(10000L)
                        .discountPrice(8000L)
                        .discountRate(BigDecimal.valueOf(20))
                        .accumulatedPoint(100L)
                        .accumulationRate(BigDecimal.valueOf(1))
                        .popularity(0L)
                        .status(EntityStatus.ACTIVE)
                        .orderNum(id)
                        .optionIds(List.of())
                        .build()
        );
        pricePolicy.addProduct(product);
        return pricePolicy;
    }

    private Product createProduct(Long id) {
        return Product.from(
                ProductSnapshotState.builder()
                        .id(id)
                        .sellerId(1L)
                        .name("테스트 상품 " + id)
                        .brandName("테스트 브랜드")
                        .detail("상세")
                        .sales(10)
                        .viewCount(100L)
                        .popularity(50L)
                        .findAllOptionsYn(false)
                        .productTags(List.of())
                        .orderNum(id)
                        .status(EntityStatus.ACTIVE)
                        .build()
        );
    }

    private void stubEnrichment(Long... productIds) {
        List<Long> productIdList = List.of(productIds);
        lenient().when(findProductReviewAggregatesPort.findByProductIds(productIdList))
                .thenReturn(Map.of());
        lenient().when(findProductImagesPort.findImagesByProductIdAndSort(any(), any()))
                .thenReturn(Optional.empty());
        lenient().when(getProductInventoryUseCase.getProductStocks(any()))
                .thenReturn(Map.of());

        stubExecutorToRunDirectly();
    }

    private void stubExecutorToRunDirectly() {
        lenient().doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(productImageExecutor).execute(any());
    }
}
