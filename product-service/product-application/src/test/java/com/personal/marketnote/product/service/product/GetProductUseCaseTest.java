package com.personal.marketnote.product.service.product;

import com.personal.marketnote.common.application.file.port.in.result.GetFilesResult;
import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.product.domain.option.ProductOption;
import com.personal.marketnote.product.domain.option.ProductOptionCategory;
import com.personal.marketnote.product.domain.option.ProductOptionCategorySnapshotState;
import com.personal.marketnote.product.domain.option.ProductOptionSnapshotState;
import com.personal.marketnote.product.domain.pricepolicy.PricePolicy;
import com.personal.marketnote.product.domain.pricepolicy.PricePolicySnapshotState;
import com.personal.marketnote.product.domain.pricepolicy.Rate;
import com.personal.marketnote.product.domain.product.Product;
import com.personal.marketnote.product.domain.product.ProductSearchTarget;
import com.personal.marketnote.product.domain.product.ProductSnapshotState;
import com.personal.marketnote.product.domain.product.ProductSortProperty;
import com.personal.marketnote.product.domain.shipping.ShippingPolicy;
import com.personal.marketnote.product.domain.shipping.ShippingPolicySnapshotState;
import com.personal.marketnote.product.exception.PricePolicyNotFoundException;
import com.personal.marketnote.product.exception.ProductNotFoundException;
import com.personal.marketnote.product.port.in.result.product.GetProductInfoWithOptionsResult;
import com.personal.marketnote.product.port.in.result.product.GetProductsResult;
import com.personal.marketnote.product.port.in.result.product.ProductCatalogItemResult;
import com.personal.marketnote.product.port.in.result.product.ProductItemResult;
import com.personal.marketnote.product.port.in.result.shipping.GetShippingPolicyResult;
import com.personal.marketnote.product.port.in.usecase.pricepolicy.GetPricePoliciesUseCase;
import com.personal.marketnote.product.port.in.usecase.product.GetProductInventoryUseCase;
import com.personal.marketnote.product.port.out.file.FindProductImagesPort;
import com.personal.marketnote.product.port.out.pricepolicy.FindPricePoliciesPort;
import com.personal.marketnote.product.port.out.product.FindProductPort;
import com.personal.marketnote.product.port.out.productcatalog.FindProductCatalogReadModelPort;
import com.personal.marketnote.product.port.out.productoption.FindProductOptionCategoryPort;
import com.personal.marketnote.product.port.out.shipping.FindShippingPolicyPort;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetProductUseCaseTest {
    @Mock
    private GetPricePoliciesUseCase getPricePoliciesUseCase;
    @Mock
    private GetProductInventoryUseCase getProductInventoryUseCase;
    @Mock
    private FindProductPort findProductPort;
    @Mock
    private FindProductOptionCategoryPort findProductOptionCategoryPort;
    @Mock
    private FindPricePoliciesPort findPricePoliciesPort;
    @Mock
    private FindProductImagesPort findProductImagesPort;
    @Mock
    private FindProductCatalogReadModelPort findProductCatalogReadModelPort;
    @Mock
    private FindShippingPolicyPort findShippingPolicyPort;
    @Mock
    private Executor productImageExecutor;

    @InjectMocks
    private GetProductService getProductService;

    @Test
    @DisplayName("상품 ID로 상품을 조회한다")
    void getProduct_success() {
        Product product = buildProduct(1L, false);

        when(findProductPort.findById(1L)).thenReturn(Optional.of(product));

        Product result = getProductService.getProduct(1L);

        assertThat(result).isEqualTo(product);
        verify(findProductPort).findById(1L);
    }

    @Test
    @DisplayName("존재하지 않는 상품 ID로 상품 조회 시 예외를 던진다")
    void getProduct_notFound() {
        when(findProductPort.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> getProductService.getProduct(99L))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("상품을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("가격정책 ID로 상품 상세 조회 시 존재하지 않으면 예외를 던진다")
    void getProductInfo_byPricePolicyId_notFound() {
        when(getPricePoliciesUseCase.getPricePoliciesAndOptions(List.of(777L)))
                .thenReturn(List.of());

        assertThatThrownBy(() -> getProductService.getProductInfo(777L))
                .isInstanceOf(PricePolicyNotFoundException.class);
    }

    @Test
    @DisplayName("첫 페이지 상품 목록 조회 시 ReadModel 기반으로 전체 개수와 Next Cursor를 반환한다")
    void getProducts_firstPage_returnsTotalElementsAndNextCursor() {
        ProductCatalogItemResult item1 = buildCatalogItem(100L, 10L, "상품10",
                "https://cdn/c10.jpg", List.of("https://cdn/c10-sm.jpg"), 7, 4.2f, 5);
        ProductCatalogItemResult item2 = buildCatalogItem(200L, 20L, "상품20",
                null, null, 0, 0f, 0);
        ProductCatalogItemResult itemOverflow = buildCatalogItem(300L, 30L, "상품30",
                null, null, 0, 0f, 0);

        when(findProductCatalogReadModelPort.findCatalogItems(
                isNull(), isNull(), eq(-1L), eq(3), eq(false),
                eq(ProductSortProperty.POPULARITY), eq(ProductSearchTarget.NAME), eq("키워드")
        )).thenReturn(List.of(item1, item2, itemOverflow));

        when(findProductCatalogReadModelPort.countCatalogItems(
                isNull(), eq(ProductSearchTarget.NAME), eq("키워드")
        )).thenReturn(100L);

        GetProductsResult result = getProductService.getProducts(
                null, null, -1L, 2,
                Sort.Direction.DESC, ProductSortProperty.POPULARITY,
                ProductSearchTarget.NAME, "키워드"
        );

        assertThat(result.totalElements()).isEqualTo(100L);
        assertThat(result.hasNext()).isTrue();
        assertThat(result.nextCursor()).isEqualTo(200L);
        assertThat(result.products()).hasSize(2);

        ProductItemResult first = result.products().getFirst();
        assertThat(first.getId()).isEqualTo(10L);
        assertThat(first.getCatalogImage().storageUrl()).isEqualTo("https://cdn/c10.jpg");
        assertThat(first.getCatalogImage().resizedStorageUrls()).containsExactly("https://cdn/c10-sm.jpg");
        assertThat(first.getStock()).isEqualTo(7);
        assertThat(first.getAverageRating()).isEqualTo(4.2f);
        assertThat(first.getTotalCount()).isEqualTo(5);

        ProductItemResult second = result.products().get(1);
        assertThat(second.getId()).isEqualTo(20L);
        assertThat(second.getCatalogImage()).isNull();
        assertThat(second.getStock()).isZero();
        assertThat(second.getAverageRating()).isZero();
        assertThat(second.getTotalCount()).isZero();
    }

    @Test
    @DisplayName("첫 페이지가 아니면(Cursor 값 존재) 총 개수 조회를 생략한다")
    void getProducts_withCursor_skipsTotalElements() {
        ProductCatalogItemResult item = buildCatalogItem(400L, 40L, "상품40",
                null, null, 3, 0f, 0);

        when(findProductCatalogReadModelPort.findCatalogItems(
                eq(5L), isNull(), eq(100L), eq(3), eq(true),
                eq(ProductSortProperty.ORDER_NUM), eq(ProductSearchTarget.BRAND_NAME), eq("브랜드")
        )).thenReturn(List.of(item));

        GetProductsResult result = getProductService.getProducts(
                5L, null, 100L, 2,
                Sort.Direction.ASC, ProductSortProperty.ORDER_NUM,
                ProductSearchTarget.BRAND_NAME, "브랜드"
        );

        assertThat(result.totalElements()).isNull();
        assertThat(result.hasNext()).isFalse();
        assertThat(result.nextCursor()).isEqualTo(400L);

        verify(findProductCatalogReadModelPort, never())
                .countCatalogItems(any(), any(), any());
    }

    @Test
    @DisplayName("가격 정책 ID 목록으로 조회 시 ReadModel Port 로 필터링한다")
    void getProducts_withPricePolicyIds_passesFilter() {
        ProductCatalogItemResult item = buildCatalogItem(500L, 50L, "상품50",
                "https://cdn/c50.jpg", List.of(), 9, 0f, 0);

        when(findProductCatalogReadModelPort.findCatalogItems(
                isNull(), eq(List.of(500L)), eq(-1L), eq(2), eq(false),
                eq(ProductSortProperty.POPULARITY), eq(ProductSearchTarget.NAME), isNull()
        )).thenReturn(List.of(item));

        when(findProductCatalogReadModelPort.countCatalogItems(
                isNull(), eq(ProductSearchTarget.NAME), isNull()
        )).thenReturn(1L);

        GetProductsResult result = getProductService.getProducts(
                null, List.of(500L), -1L, 1,
                Sort.Direction.DESC, ProductSortProperty.POPULARITY,
                ProductSearchTarget.NAME, null
        );

        assertThat(result.products()).hasSize(1);
        assertThat(result.products().getFirst().getStock()).isEqualTo(9);
    }

    @Test
    @DisplayName("ReadModel 조회 결과가 없으면 빈 결과를 반환한다")
    void getProducts_emptyResult_returnsEmpty() {
        when(findProductCatalogReadModelPort.findCatalogItems(
                any(), any(), any(), anyInt(), anyBoolean(), any(), any(), any()
        )).thenReturn(List.of());
        when(findProductCatalogReadModelPort.countCatalogItems(
                isNull(), eq(ProductSearchTarget.NAME), eq("키워드")
        )).thenReturn(0L);

        GetProductsResult result = getProductService.getProducts(
                null, null, -1L, 10,
                Sort.Direction.DESC, ProductSortProperty.POPULARITY,
                ProductSearchTarget.NAME, "키워드"
        );

        assertThat(result.products()).isEmpty();
        assertThat(result.hasNext()).isFalse();
        assertThat(result.nextCursor()).isNull();
        assertThat(result.totalElements()).isZero();
        verifyNoInteractions(findProductPort, findProductOptionCategoryPort,
                findProductImagesPort, getProductInventoryUseCase);
    }

    @Test
    @DisplayName("카테고리 첫 페이지 조회 시 카테고리 필터를 Port 에 전달한다")
    void getProducts_categoryFirstPage_passesCategoryFilter() {
        ProductCatalogItemResult item = buildCatalogItem(701L, 70L, "상품70",
                null, null, 4, 0f, 0);

        when(findProductCatalogReadModelPort.findCatalogItems(
                eq(7L), any(), eq(-1L), anyInt(), anyBoolean(), any(), any(), any()
        )).thenReturn(List.of(item));

        when(findProductCatalogReadModelPort.countCatalogItems(
                eq(7L), eq(ProductSearchTarget.NAME), eq("키워드")
        )).thenReturn(25L);

        GetProductsResult result = getProductService.getProducts(
                7L, null, -1L, 2,
                Sort.Direction.DESC, ProductSortProperty.POPULARITY,
                ProductSearchTarget.NAME, "키워드"
        );

        assertThat(result.totalElements()).isEqualTo(25L);
        assertThat(result.nextCursor()).isEqualTo(701L);
        assertThat(result.products()).hasSize(1);
    }

    @Test
    @DisplayName("ACCUMULATED_POINT_RATE 정렬로 첫 페이지 조회 시 정렬 속성을 Port 에 전달한다")
    void getProducts_accumulatedPointRateDescFirstPage() {
        ProductCatalogItemResult item1 = buildCatalogItem(1201L, 120L, "상품120",
                null, null, 5, 0f, 0);
        ProductCatalogItemResult item2 = buildCatalogItem(1301L, 130L, "상품130",
                null, null, 3, 0f, 0);
        ProductCatalogItemResult itemOverflow = buildCatalogItem(1401L, 140L, "상품140",
                null, null, 0, 0f, 0);

        when(findProductCatalogReadModelPort.findCatalogItems(
                isNull(), isNull(), eq(-1L), anyInt(), anyBoolean(),
                eq(ProductSortProperty.ACCUMULATED_POINT_RATE), any(), isNull()
        )).thenReturn(List.of(item1, item2, itemOverflow));

        when(findProductCatalogReadModelPort.countCatalogItems(
                isNull(), eq(ProductSearchTarget.NAME), isNull()
        )).thenReturn(50L);

        GetProductsResult result = getProductService.getProducts(
                null, null, -1L, 2,
                Sort.Direction.DESC, ProductSortProperty.ACCUMULATED_POINT_RATE,
                ProductSearchTarget.NAME, null
        );

        assertThat(result.totalElements()).isEqualTo(50L);
        assertThat(result.hasNext()).isTrue();
        assertThat(result.nextCursor()).isEqualTo(1301L);
        assertThat(result.products()).hasSize(2);

        verify(findProductCatalogReadModelPort).findCatalogItems(
                isNull(), isNull(), eq(-1L), anyInt(), eq(false),
                eq(ProductSortProperty.ACCUMULATED_POINT_RATE),
                eq(ProductSearchTarget.NAME), isNull()
        );
    }

    @Test
    @DisplayName("상품 상세 조회 시 판매자의 배송비 정책이 존재하면 배송비 정보를 포함한다")
    void getProductInfo_withShippingPolicy_includesShippingInfo() {
        stubProductImageExecutor();
        Product product = buildProduct(7L, false);
        PricePolicy defaultPolicy = buildPricePolicy(830L, product, null, List.of());

        when(findProductPort.findActiveById(7L)).thenReturn(Optional.of(product));
        when(findProductOptionCategoryPort.findActiveWithOptionsByProductId(7L))
                .thenReturn(List.of());
        when(findPricePoliciesPort.findByProductId(7L)).thenReturn(List.of(defaultPolicy));
        when(getProductInventoryUseCase.getProductStocks(List.of(830L)))
                .thenReturn(Map.of(830L, 10));
        stubProductImagesEmpty(7L);

        ShippingPolicy shippingPolicy = buildShippingPolicy(1L, 1L, "한진택배", 3000L, 20000L);
        when(findShippingPolicyPort.findActiveBySellerId(1L))
                .thenReturn(Optional.of(shippingPolicy));

        GetProductInfoWithOptionsResult result = getProductService.getProductInfo(7L, List.of());

        GetShippingPolicyResult shippingResult = result.productInfo().shippingPolicy();
        assertThat(shippingResult).isNotNull();
        assertThat(shippingResult.deliveryCompany()).isEqualTo("한진택배");
        assertThat(shippingResult.shippingFee()).isEqualTo(3000L);
        assertThat(shippingResult.freeShippingThreshold()).isEqualTo(20000L);
    }

    @Test
    @DisplayName("상품 상세 조회 시 판매자의 배송비 정책이 미등록이면 배송비 정보가 null이다")
    void getProductInfo_withoutShippingPolicy_shippingInfoIsNull() {
        stubProductImageExecutor();
        Product product = buildProduct(8L, false);
        PricePolicy defaultPolicy = buildPricePolicy(840L, product, null, List.of());

        when(findProductPort.findActiveById(8L)).thenReturn(Optional.of(product));
        when(findProductOptionCategoryPort.findActiveWithOptionsByProductId(8L))
                .thenReturn(List.of());
        when(findPricePoliciesPort.findByProductId(8L)).thenReturn(List.of(defaultPolicy));
        when(getProductInventoryUseCase.getProductStocks(List.of(840L)))
                .thenReturn(Map.of(840L, 5));
        stubProductImagesEmpty(8L);

        when(findShippingPolicyPort.findActiveBySellerId(1L))
                .thenReturn(Optional.empty());

        GetProductInfoWithOptionsResult result = getProductService.getProductInfo(8L, List.of());

        assertThat(result.productInfo().shippingPolicy()).isNull();
    }

    @Test
    @DisplayName("가격 정책 ID로 상품 상세 조회 시 배송비 정보를 포함한다")
    void getProductInfo_byPricePolicyId_includesShippingInfo() {
        stubProductImageExecutor();
        Product product = buildProduct(9L, true);
        ProductOption option = buildOption(601L, "옵션");
        List<ProductOption> optionList = List.of(option);

        PricePolicy policyForLookup = buildPricePolicy(850L, product, null, optionList);
        when(getPricePoliciesUseCase.getPricePoliciesAndOptions(List.of(850L)))
                .thenReturn(List.of(policyForLookup));

        when(findProductPort.findActiveById(9L)).thenReturn(Optional.of(product));
        ProductOptionCategory category = buildOptionCategory(61L, product, optionList);
        when(findProductOptionCategoryPort.findActiveWithOptionsByProductId(9L))
                .thenReturn(List.of(category));

        PricePolicy defaultPolicy = buildPricePolicy(849L, product, null, List.of());
        PricePolicy selectedPolicy = buildPricePolicy(850L, product, List.of(601L), List.of());
        when(findPricePoliciesPort.findByProductId(9L)).thenReturn(List.of(defaultPolicy, selectedPolicy));

        when(getProductInventoryUseCase.getProductStocks(List.of(850L)))
                .thenReturn(Map.of(850L, 8));
        stubProductImagesEmpty(9L);

        ShippingPolicy shippingPolicy = buildShippingPolicy(2L, 1L, "CJ대한통운", 2500L, 30000L);
        when(findShippingPolicyPort.findActiveBySellerId(1L))
                .thenReturn(Optional.of(shippingPolicy));

        GetProductInfoWithOptionsResult result = getProductService.getProductInfo(850L);

        GetShippingPolicyResult shippingResult = result.productInfo().shippingPolicy();
        assertThat(shippingResult).isNotNull();
        assertThat(shippingResult.deliveryCompany()).isEqualTo("CJ대한통운");
        assertThat(shippingResult.shippingFee()).isEqualTo(2500L);
        assertThat(shippingResult.freeShippingThreshold()).isEqualTo(30000L);
    }

    private ProductCatalogItemResult buildCatalogItem(
            Long pricePolicyId, Long productId, String name,
            String representativeImageUrl, List<String> resizedImageUrls,
            int stockQuantity, float reviewAverageRating, int reviewCount
    ) {
        return new ProductCatalogItemResult(
                pricePolicyId,
                productId,
                1L,
                null,
                name,
                "브랜드",
                10_000L,
                9_000L,
                BigDecimal.valueOf(10.0),
                100L,
                BigDecimal.ONE,
                0L,
                0L,
                representativeImageUrl,
                resizedImageUrls,
                stockQuantity,
                reviewCount,
                reviewAverageRating, 0L, java.util.List.of(), java.util.List.of(),
                EntityStatus.ACTIVE.name()
        );
    }

    private Product buildProduct(Long id, boolean findAllOptions) {
        return Product.from(
                ProductSnapshotState.builder()
                        .id(id)
                        .sellerId(1L)
                        .name("상품-" + id)
                        .brandName("브랜드-" + id)
                        .detail("설명-" + id)
                        .findAllOptionsYn(findAllOptions)
                        .productTags(List.of())
                        .status(EntityStatus.ACTIVE)
                        .build()
        );
    }

    private ProductOption buildOption(Long id, String content) {
        return ProductOption.from(
                ProductOptionSnapshotState.builder()
                        .id(id)
                        .content(content)
                        .status(EntityStatus.ACTIVE)
                        .build()
        );
    }

    private ProductOptionCategory buildOptionCategory(Long id, Product product, List<ProductOption> options) {
        return ProductOptionCategory.from(
                ProductOptionCategorySnapshotState.builder()
                        .id(id)
                        .product(product)
                        .name("카테고리-" + id)
                        .options(options)
                        .status(EntityStatus.ACTIVE)
                        .build()
        );
    }

    private PricePolicy buildPricePolicy(
            Long id,
            Product product,
            List<Long> optionIds,
            List<ProductOption> productOptions
    ) {
        return PricePolicy.from(
                PricePolicySnapshotState.builder()
                        .id(id)
                        .product(product)
                        .price(10000L)
                        .discountPrice(9000L)
                        .discountRate(Rate.of(BigDecimal.valueOf(10)))
                        .accumulatedPoint(100L)
                        .accumulationRate(Rate.of(BigDecimal.valueOf(1)))
                        .status(EntityStatus.ACTIVE)
                        .optionIds(optionIds)
                        .productOptions(productOptions)
                        .build()
        );
    }

    private ShippingPolicy buildShippingPolicy(
            Long id, Long sellerId, String deliveryCompany, Long shippingFee, Long freeShippingThreshold
    ) {
        return ShippingPolicy.from(
                ShippingPolicySnapshotState.builder()
                        .id(id)
                        .sellerId(sellerId)
                        .deliveryCompany(deliveryCompany)
                        .shippingFee(shippingFee)
                        .freeShippingThreshold(freeShippingThreshold)
                        .status(EntityStatus.ACTIVE)
                        .build()
        );
    }

    private void stubProductImageExecutor() {
        doAnswer(invocation -> {
            Runnable task = invocation.getArgument(0);
            task.run();
            return null;
        }).when(productImageExecutor).execute(any(Runnable.class));
    }

    private void stubProductImagesEmpty(Long productId) {
        when(findProductImagesPort.findImagesByProductIdAndSort(productId,
                com.personal.marketnote.common.domain.file.FileSort.PRODUCT_REPRESENTATIVE_IMAGE))
                .thenReturn(Optional.empty());
        when(findProductImagesPort.findImagesByProductIdAndSort(productId,
                com.personal.marketnote.common.domain.file.FileSort.PRODUCT_CONTENT_IMAGE))
                .thenReturn(Optional.empty());
    }
}
