package com.personal.marketnote.product.service.product;

import com.personal.marketnote.common.adapter.out.persistence.audit.EntityStatus;
import com.personal.marketnote.common.application.file.port.in.result.GetFileResult;
import com.personal.marketnote.common.application.file.port.in.result.GetFilesResult;
import com.personal.marketnote.common.domain.file.FileSort;
import com.personal.marketnote.product.domain.option.ProductOption;
import com.personal.marketnote.product.domain.option.ProductOptionCategory;
import com.personal.marketnote.product.domain.option.ProductOptionCategorySnapshotState;
import com.personal.marketnote.product.domain.option.ProductOptionSnapshotState;
import com.personal.marketnote.product.domain.pricepolicy.PricePolicy;
import com.personal.marketnote.product.domain.pricepolicy.PricePolicySnapshotState;
import com.personal.marketnote.product.domain.product.Product;
import com.personal.marketnote.product.domain.product.ProductSearchTarget;
import com.personal.marketnote.product.domain.product.ProductSnapshotState;
import com.personal.marketnote.product.domain.product.ProductSortProperty;
import com.personal.marketnote.product.exception.PricePolicyNotFoundException;
import com.personal.marketnote.product.exception.ProductNotFoundException;
import com.personal.marketnote.product.port.in.result.product.GetProductInfoWithOptionsResult;
import com.personal.marketnote.product.port.in.result.product.GetProductsResult;
import com.personal.marketnote.product.port.in.result.product.ProductItemResult;
import com.personal.marketnote.product.port.in.usecase.pricepolicy.GetPricePoliciesUseCase;
import com.personal.marketnote.product.port.in.usecase.product.GetProductInventoryUseCase;
import com.personal.marketnote.product.port.out.file.FindProductImagesPort;
import com.personal.marketnote.product.port.out.pricepolicy.FindPricePoliciesPort;
import com.personal.marketnote.product.port.out.product.FindProductPort;
import com.personal.marketnote.product.port.out.productoption.FindProductOptionCategoryPort;
import com.personal.marketnote.product.port.out.result.ProductReviewAggregateResult;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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
    private FindProductReviewAggregatesPort findProductReviewAggregatesPort;
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
    @DisplayName("가격 정책 ID로 상품 상세 정보를 조회할 때 가격 정책이 없으면 예외를 던진다")
    void getProductInfo_byPricePolicyId_notFound() {
        when(getPricePoliciesUseCase.getPricePoliciesAndOptions(List.of(10L))).thenReturn(List.of());

        assertThatThrownBy(() -> getProductService.getProductInfo(10L))
                .isInstanceOf(PricePolicyNotFoundException.class)
                .hasMessageContaining("가격 정책을 찾을 수 없습니다");

        verifyNoInteractions(
                findProductPort,
                findProductOptionCategoryPort,
                findPricePoliciesPort,
                findProductImagesPort,
                getProductInventoryUseCase,
                findProductReviewAggregatesPort
        );
    }

    @Test
    @DisplayName("가격 정책 ID로 상품 상세 정보 조회 시 옵션 가격 정책을 선택한다")
    void getProductInfo_byPricePolicyId_selectsOptionPricePolicy() {
        stubProductImageExecutor();
        Product product = buildProduct(1L, true);
        ProductOption option1 = buildOption(101L, "옵션1");
        ProductOption option2 = buildOption(102L, "옵션2");
        List<ProductOption> optionList = List.of(option1, option2);

        PricePolicy policyForLookup = buildPricePolicy(500L, product, null, optionList);
        when(getPricePoliciesUseCase.getPricePoliciesAndOptions(List.of(500L)))
                .thenReturn(List.of(policyForLookup));

        when(findProductPort.findActiveById(1L)).thenReturn(Optional.of(product));
        ProductOptionCategory category = buildOptionCategory(11L, product, optionList);
        when(findProductOptionCategoryPort.findActiveWithOptionsByProductId(1L))
                .thenReturn(List.of(category));

        PricePolicy defaultPolicy = buildPricePolicy(400L, product, null, List.of());
        PricePolicy selectedPolicy = buildPricePolicy(500L, product, List.of(101L, 102L), List.of());
        when(findPricePoliciesPort.findByProductId(1L)).thenReturn(List.of(defaultPolicy, selectedPolicy));

        when(getProductInventoryUseCase.getProductStocks(List.of(500L)))
                .thenReturn(Map.of(500L, 12));

        GetFilesResult representativeImages = buildFilesResult(1L, "rep.jpg");
        GetFilesResult contentImages = buildFilesResult(2L, "content.jpg");
        when(findProductImagesPort.findImagesByProductIdAndSort(1L, FileSort.PRODUCT_REPRESENTATIVE_IMAGE))
                .thenReturn(Optional.of(representativeImages));
        when(findProductImagesPort.findImagesByProductIdAndSort(1L, FileSort.PRODUCT_CONTENT_IMAGE))
                .thenReturn(Optional.of(contentImages));

        GetProductInfoWithOptionsResult result = getProductService.getProductInfo(500L);

        assertThat(result.productInfo().selectedPricePolicy().id()).isEqualTo(500L);
        assertThat(result.productInfo().stock()).isEqualTo(12);
        assertThat(result.categories()).hasSize(1);
        assertThat(result.categories().getFirst().options())
                .extracting(option -> option.isSelected())
                .containsOnly(true);
        assertThat(result.representativeImages()).isEqualTo(representativeImages);
        assertThat(result.contentImages()).isEqualTo(contentImages);
        assertThat(result.pricePolicies())
                .extracting(pricePolicy -> pricePolicy.id())
                .containsExactlyInAnyOrder(400L, 500L);
    }

    @Test
    @DisplayName("상품 상세 정보 조회 시 기본 가격 정책이 없으면 예외를 던진다")
    void getProductInfo_missingDefaultPolicy_throws() {
        stubProductImageExecutor();
        Product product = buildProduct(2L, false);
        PricePolicy optionPolicy = buildPricePolicy(900L, product, List.of(1L), List.of());

        when(findProductPort.findActiveById(2L)).thenReturn(Optional.of(product));
        when(findProductOptionCategoryPort.findActiveWithOptionsByProductId(2L))
                .thenReturn(List.of());
        when(findPricePoliciesPort.findByProductId(2L)).thenReturn(List.of(optionPolicy));
        when(findProductImagesPort.findImagesByProductIdAndSort(2L, FileSort.PRODUCT_REPRESENTATIVE_IMAGE))
                .thenReturn(Optional.empty());
        when(findProductImagesPort.findImagesByProductIdAndSort(2L, FileSort.PRODUCT_CONTENT_IMAGE))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> getProductService.getProductInfo(2L, List.of()))
                .isInstanceOf(PricePolicyNotFoundException.class)
                .hasMessageContaining("가격 정책을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("상품 상세 정보 조회 시 상품이 없으면 예외를 던진다")
    void getProductInfo_productNotFound_throws() {
        when(findProductPort.findActiveById(77L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> getProductService.getProductInfo(77L, List.of()))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("상품을 찾을 수 없습니다");

        verify(findProductPort).findActiveById(77L);
        verifyNoInteractions(findProductOptionCategoryPort, findPricePoliciesPort, getProductInventoryUseCase);
    }

    @Test
    @DisplayName("상품 상세 정보 조회 시 선택된 옵션이 가격 정책과 불일치하면 기본 가격 정책을 사용한다")
    void getProductInfo_optionMismatch_usesDefaultPolicy() {
        stubProductImageExecutor();
        Product product = buildProduct(3L, true);
        ProductOption option1 = buildOption(201L, "옵션-A");
        ProductOptionCategory category = buildOptionCategory(21L, product, List.of(option1));
        PricePolicy defaultPolicy = buildPricePolicy(700L, product, null, List.of());
        PricePolicy optionPolicy = buildPricePolicy(701L, product, List.of(202L), List.of());

        when(findProductPort.findActiveById(3L)).thenReturn(Optional.of(product));
        when(findProductOptionCategoryPort.findActiveWithOptionsByProductId(3L))
                .thenReturn(List.of(category));
        when(findPricePoliciesPort.findByProductId(3L)).thenReturn(List.of(defaultPolicy, optionPolicy));
        when(getProductInventoryUseCase.getProductStocks(List.of(700L)))
                .thenReturn(Map.of(700L, 5));
        when(findProductImagesPort.findImagesByProductIdAndSort(3L, FileSort.PRODUCT_REPRESENTATIVE_IMAGE))
                .thenReturn(Optional.empty());
        when(findProductImagesPort.findImagesByProductIdAndSort(3L, FileSort.PRODUCT_CONTENT_IMAGE))
                .thenReturn(Optional.empty());

        GetProductInfoWithOptionsResult result = getProductService.getProductInfo(3L, List.of(201L));

        assertThat(result.productInfo().selectedPricePolicy().id()).isEqualTo(700L);
        assertThat(result.representativeImages()).isNull();
        assertThat(result.contentImages()).isNull();
    }

    @Test
    @DisplayName("상품 상세 정보 조회 시 옵션 개별 노출이 꺼진 상품은 기본 가격 정책을 사용한다")
    void getProductInfo_findAllOptionsDisabled_usesDefaultPolicy() {
        stubProductImageExecutor();
        Product product = buildProduct(4L, false);
        ProductOption option = buildOption(301L, "옵션");
        ProductOptionCategory category = buildOptionCategory(41L, product, List.of(option));
        List<Long> selectedOptionIds = List.of(301L);
        PricePolicy defaultPolicy = buildPricePolicy(800L, product, null, List.of());
        PricePolicy optionPolicy = buildPricePolicy(801L, product, selectedOptionIds, List.of());

        when(findProductPort.findActiveById(4L)).thenReturn(Optional.of(product));
        when(findProductOptionCategoryPort.findActiveWithOptionsByProductId(4L))
                .thenReturn(List.of(category));
        when(findPricePoliciesPort.findByProductId(4L)).thenReturn(List.of(defaultPolicy, optionPolicy));
        when(getProductInventoryUseCase.getProductStocks(List.of(800L)))
                .thenReturn(Map.of(800L, 9));
        stubProductImagesEmpty(4L);

        GetProductInfoWithOptionsResult result = getProductService.getProductInfo(4L, selectedOptionIds);

        assertThat(result.productInfo().selectedPricePolicy().id()).isEqualTo(800L);
        assertThat(result.productInfo().stock()).isEqualTo(9);
    }

    @Test
    @DisplayName("상품 상세 정보 조회 시 선택된 옵션이 없으면 기본 가격 정책을 사용한다")
    void getProductInfo_emptySelectedOptions_usesDefaultPolicy() {
        stubProductImageExecutor();
        Product product = buildProduct(5L, true);
        ProductOption option = buildOption(401L, "옵션");
        ProductOptionCategory category = buildOptionCategory(51L, product, List.of(option));
        PricePolicy defaultPolicy = buildPricePolicy(810L, product, null, List.of());
        PricePolicy optionPolicy = buildPricePolicy(811L, product, List.of(401L), List.of());

        when(findProductPort.findActiveById(5L)).thenReturn(Optional.of(product));
        when(findProductOptionCategoryPort.findActiveWithOptionsByProductId(5L))
                .thenReturn(List.of(category));
        when(findPricePoliciesPort.findByProductId(5L)).thenReturn(List.of(defaultPolicy, optionPolicy));
        when(getProductInventoryUseCase.getProductStocks(List.of(810L)))
                .thenReturn(Map.of(810L, 2));
        stubProductImagesEmpty(5L);

        GetProductInfoWithOptionsResult result = getProductService.getProductInfo(5L, List.of());

        assertThat(result.productInfo().selectedPricePolicy().id()).isEqualTo(810L);
        assertThat(result.categories().getFirst().options())
                .extracting(optionItem -> optionItem.isSelected())
                .containsOnly(false);
    }

    @Test
    @DisplayName("상품 상세 정보 조회 시 옵션 카테고리가 없으면 기본 가격 정책을 사용한다")
    void getProductInfo_emptyCategories_usesDefaultPolicy() {
        stubProductImageExecutor();
        Product product = buildProduct(6L, true);
        List<Long> selectedOptionIds = List.of(501L);
        PricePolicy defaultPolicy = buildPricePolicy(820L, product, null, List.of());
        PricePolicy optionPolicy = buildPricePolicy(821L, product, selectedOptionIds, List.of());

        when(findProductPort.findActiveById(6L)).thenReturn(Optional.of(product));
        when(findProductOptionCategoryPort.findActiveWithOptionsByProductId(6L))
                .thenReturn(List.of());
        when(findPricePoliciesPort.findByProductId(6L)).thenReturn(List.of(defaultPolicy, optionPolicy));
        when(getProductInventoryUseCase.getProductStocks(List.of(820L)))
                .thenReturn(Map.of(820L, 1));
        stubProductImagesEmpty(6L);

        GetProductInfoWithOptionsResult result = getProductService.getProductInfo(6L, selectedOptionIds);

        assertThat(result.productInfo().selectedPricePolicy().id()).isEqualTo(820L);
        assertThat(result.categories()).isEmpty();
    }

    @Test
    @DisplayName("첫 페이지 상품 목록 조회 시 전체 개수와 Next Cursor를 포함한다")
    void getProducts_firstPage_includesTotalElementsAndNextCursor() {
        stubProductImageExecutor();
        Product product1 = buildProduct(10L, false);
        Product product2 = buildProduct(20L, false);
        Product product3 = buildProduct(30L, false);

        PricePolicy policy1 = buildPricePolicy(100L, product1, null, List.of());
        PricePolicy policy2 = buildPricePolicy(200L, product2, null, List.of());
        PricePolicy policy3 = buildPricePolicy(300L, product3, null, List.of());

        when(findPricePoliciesPort.findPricePolicies(
                any(), any(), any(), any(), any(), any()
        )).thenReturn(List.of(policy1, policy2, policy3));

        when(findProductPort.findById(10L)).thenReturn(Optional.of(product1));
        when(findProductPort.findById(20L)).thenReturn(Optional.of(product2));

        when(getProductInventoryUseCase.getProductStocks(List.of(100L, 200L)))
                .thenReturn(Map.of(100L, 7, 200L, 0));

        GetFilesResult catalogImages = buildFilesResult(11L, "catalog.jpg");
        when(findProductImagesPort.findImagesByProductIdAndSort(anyLong(), eq(FileSort.PRODUCT_CATALOG_IMAGE)))
                .thenReturn(Optional.empty());
        when(findProductImagesPort.findImagesByProductIdAndSort(10L, FileSort.PRODUCT_CATALOG_IMAGE))
                .thenReturn(Optional.of(catalogImages));

        when(findProductReviewAggregatesPort.findByProductIds(List.of(10L, 20L)))
                .thenReturn(Map.of(
                        10L, new ProductReviewAggregateResult(10L, 5, 4.2f)
                ));

        when(findPricePoliciesPort.countActivePricePoliciesByCategoryId(
                isNull(), eq(ProductSearchTarget.NAME), eq("키워드")
        )).thenReturn(100L);

        GetProductsResult result = getProductService.getProducts(
                null,
                null,
                null,
                2,
                Sort.Direction.DESC,
                ProductSortProperty.POPULARITY,
                ProductSearchTarget.NAME,
                "키워드"
        );

        assertThat(result.totalElements()).isEqualTo(100L);
        assertThat(result.hasNext()).isTrue();
        assertThat(result.nextCursor()).isEqualTo(200L);
        assertThat(result.products()).hasSize(2);

        ProductItemResult first = result.products().getFirst();
        assertThat(first.getId()).isEqualTo(10L);
        assertThat(first.getCatalogImage()).isEqualTo(catalogImages.images().getFirst());
        assertThat(first.getStock()).isEqualTo(7);
        assertThat(first.getAverageRating()).isEqualTo(4.2f);
        assertThat(first.getTotalCount()).isEqualTo(5);

        ProductItemResult second = result.products().get(1);
        assertThat(second.getId()).isEqualTo(20L);
        assertThat(second.getCatalogImage()).isNull();
        assertThat(second.getStock()).isEqualTo(0);
        assertThat(second.getAverageRating()).isEqualTo(0f);
        assertThat(second.getTotalCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("첫 페이지가 아니면(Cursor 값 존재) 총 개수 조회를 생략한다")
    void getProducts_withCategoryAndCursor_skipsTotalElements() {
        stubProductImageExecutor();
        Product product = buildProduct(40L, false);
        PricePolicy policy = buildPricePolicy(400L, product, null, List.of());

        when(findPricePoliciesPort.findPricePoliciesByCategoryId(
                eq(5L), any(), any(), any(), any(), any(), any()
        )).thenReturn(List.of(policy));
        when(findProductPort.findById(40L)).thenReturn(Optional.of(product));
        when(findProductImagesPort.findImagesByProductIdAndSort(anyLong(), eq(FileSort.PRODUCT_CATALOG_IMAGE)))
                .thenReturn(Optional.empty());
        when(getProductInventoryUseCase.getProductStocks(List.of(400L)))
                .thenReturn(Map.of(400L, 3));

        GetProductsResult result = getProductService.getProducts(
                5L,
                null,
                100L,
                2,
                Sort.Direction.ASC,
                ProductSortProperty.ORDER_NUM,
                ProductSearchTarget.BRAND_NAME,
                "브랜드"
        );

        assertThat(result.totalElements()).isNull();
        assertThat(result.hasNext()).isFalse();
        assertThat(result.nextCursor()).isEqualTo(400L);

        verify(findPricePoliciesPort, never())
                .countActivePricePoliciesByCategoryId(any(), any(), any());
    }

    @Test
    @DisplayName("가격 정책 ID 목록으로 조회 시 재고 조회를 생략한다")
    void getProducts_withPricePolicyIds_skipsInventoryLookup() {
        stubProductImageExecutor();
        Product product = buildProduct(50L, false);
        PricePolicy policy = buildPricePolicy(500L, product, null, List.of());

        when(findPricePoliciesPort.findPricePolicies(
                any(), any(), any(), any(), any(), any()
        )).thenReturn(List.of(policy));
        when(findProductPort.findById(50L)).thenReturn(Optional.of(product));
        when(findProductImagesPort.findImagesByProductIdAndSort(anyLong(), eq(FileSort.PRODUCT_CATALOG_IMAGE)))
                .thenReturn(Optional.empty());
        when(findPricePoliciesPort.countActivePricePoliciesByCategoryId(
                isNull(), eq(ProductSearchTarget.NAME), isNull()
        )).thenReturn(1L);

        GetProductsResult result = getProductService.getProducts(
                null,
                List.of(500L),
                null,
                1,
                Sort.Direction.DESC,
                ProductSortProperty.POPULARITY,
                ProductSearchTarget.NAME,
                null
        );

        assertThat(result.products()).hasSize(1);
        assertThat(result.products().getFirst().getStock()).isNull();
        verifyNoInteractions(getProductInventoryUseCase);
    }

    @Test
    @DisplayName("옵션 개별 노출 상품은 선택된 옵션 정보를 포함한다")
    void getProducts_findAllOptions_includesSelectedOptions() {
        stubProductImageExecutor();
        Product product = buildProduct(60L, true);
        ProductOption option1 = buildOption(301L, "레드");
        ProductOption option2 = buildOption(302L, "블루");
        ProductOptionCategory category = buildOptionCategory(31L, product, List.of(option1, option2));
        PricePolicy policy = buildPricePolicy(600L, product, List.of(301L, 302L), List.of());

        when(findPricePoliciesPort.findPricePolicies(
                any(), any(), any(), any(), any(), any()
        )).thenReturn(List.of(policy));
        when(findProductPort.findById(60L)).thenReturn(Optional.of(product));
        when(findProductOptionCategoryPort.findActiveWithOptionsByProductId(60L))
                .thenReturn(List.of(category));
        when(findPricePoliciesPort.countActivePricePoliciesByCategoryId(
                isNull(), eq(ProductSearchTarget.NAME), eq("옵션")
        )).thenReturn(1L);
        when(getProductInventoryUseCase.getProductStocks(List.of(600L)))
                .thenReturn(Map.of(600L, 3));
        when(findProductImagesPort.findImagesByProductIdAndSort(anyLong(), eq(FileSort.PRODUCT_CATALOG_IMAGE)))
                .thenReturn(Optional.empty());

        GetProductsResult result = getProductService.getProducts(
                null,
                null,
                null,
                1,
                Sort.Direction.DESC,
                ProductSortProperty.POPULARITY,
                ProductSearchTarget.NAME,
                "옵션"
        );

        assertThat(result.products()).hasSize(1);
        assertThat(result.products().getFirst().getSelectedOptions())
                .extracting(option -> option.id())
                .containsExactlyInAnyOrder(301L, 302L);
    }

    @Test
    @DisplayName("상품 목록 조회 결과가 없으면 빈 결과를 반환한다")
    void getProducts_emptyResult_returnsEmpty() {
        when(findPricePoliciesPort.findPricePolicies(
                any(), any(), any(), any(), any(), any()
        )).thenReturn(List.of());
        when(getProductInventoryUseCase.getProductStocks(List.of()))
                .thenReturn(Map.of());
        when(findPricePoliciesPort.countActivePricePoliciesByCategoryId(
                isNull(), eq(ProductSearchTarget.NAME), eq("키워드")
        )).thenReturn(0L);

        GetProductsResult result = getProductService.getProducts(
                null,
                null,
                null,
                10,
                Sort.Direction.DESC,
                ProductSortProperty.POPULARITY,
                ProductSearchTarget.NAME,
                "키워드"
        );

        assertThat(result.products()).isEmpty();
        assertThat(result.hasNext()).isFalse();
        assertThat(result.nextCursor()).isNull();
        assertThat(result.totalElements()).isEqualTo(0L);
        verifyNoInteractions(findProductPort, findProductOptionCategoryPort, findProductImagesPort, findProductReviewAggregatesPort);
    }

    @Test
    @DisplayName("카테고리 첫 페이지 상품 목록 조회 시 전체 개수를 포함한다")
    void getProducts_categoryFirstPage_includesTotalElements() {
        stubProductImageExecutor();
        Long categoryId = 7L;
        Product product = buildProduct(70L, false);
        PricePolicy policy1 = buildPricePolicy(701L, product, null, List.of());
        PricePolicy policy2 = buildPricePolicy(702L, product, null, List.of());

        when(findPricePoliciesPort.findPricePoliciesByCategoryId(
                eq(categoryId), any(), any(), any(), any(), any(), any()
        )).thenReturn(List.of(policy1, policy2));
        when(findProductPort.findById(70L)).thenReturn(Optional.of(product));
        when(getProductInventoryUseCase.getProductStocks(List.of(701L)))
                .thenReturn(Map.of(701L, 4));
        when(findProductImagesPort.findImagesByProductIdAndSort(anyLong(), eq(FileSort.PRODUCT_CATALOG_IMAGE)))
                .thenReturn(Optional.empty());
        when(findPricePoliciesPort.countActivePricePoliciesByCategoryId(
                eq(categoryId), eq(ProductSearchTarget.NAME), eq("키워드")
        )).thenReturn(25L);

        GetProductsResult result = getProductService.getProducts(
                categoryId,
                null,
                null,
                1,
                Sort.Direction.DESC,
                ProductSortProperty.POPULARITY,
                ProductSearchTarget.NAME,
                "키워드"
        );

        assertThat(result.totalElements()).isEqualTo(25L);
        assertThat(result.hasNext()).isTrue();
        assertThat(result.nextCursor()).isEqualTo(701L);
        assertThat(result.products()).hasSize(1);
    }

    @Test
    @DisplayName("옵션 ID가 비어 있는 경우 기본 상품 정보를 반환한다")
    void getProducts_findAllOptionsWithEmptyOptionIds_usesDefaultMapping() {
        stubProductImageExecutor();
        Product product = buildProduct(80L, true);
        PricePolicy policy = buildPricePolicy(801L, product, List.of(), List.of());

        when(findPricePoliciesPort.findPricePolicies(
                any(), any(), any(), any(), any(), any()
        )).thenReturn(List.of(policy));
        when(findProductPort.findById(80L)).thenReturn(Optional.of(product));
        when(getProductInventoryUseCase.getProductStocks(List.of(801L)))
                .thenReturn(Map.of(801L, 6));
        when(findProductImagesPort.findImagesByProductIdAndSort(anyLong(), eq(FileSort.PRODUCT_CATALOG_IMAGE)))
                .thenReturn(Optional.empty());

        GetProductsResult result = getProductService.getProducts(
                null,
                null,
                10L,
                1,
                Sort.Direction.DESC,
                ProductSortProperty.POPULARITY,
                ProductSearchTarget.NAME,
                null
        );

        assertThat(result.products()).hasSize(1);
        assertThat(result.products().getFirst().getSelectedOptions()).isNull();
        verifyNoInteractions(findProductOptionCategoryPort);
    }

    @Test
    @DisplayName("옵션 카테고리가 없으면 기본 상품 정보를 반환한다")
    void getProducts_findAllOptionsWithEmptyCategories_usesDefaultMapping() {
        stubProductImageExecutor();
        Product product = buildProduct(90L, true);
        PricePolicy policy = buildPricePolicy(901L, product, List.of(1L), List.of());

        when(findPricePoliciesPort.findPricePolicies(
                any(), any(), any(), any(), any(), any()
        )).thenReturn(List.of(policy));
        when(findProductPort.findById(90L)).thenReturn(Optional.of(product));
        when(findProductOptionCategoryPort.findActiveWithOptionsByProductId(90L))
                .thenReturn(List.of());
        when(getProductInventoryUseCase.getProductStocks(List.of(901L)))
                .thenReturn(Map.of(901L, 8));
        when(findProductImagesPort.findImagesByProductIdAndSort(anyLong(), eq(FileSort.PRODUCT_CATALOG_IMAGE)))
                .thenReturn(Optional.empty());

        GetProductsResult result = getProductService.getProducts(
                null,
                null,
                10L,
                1,
                Sort.Direction.DESC,
                ProductSortProperty.POPULARITY,
                ProductSearchTarget.NAME,
                null
        );

        assertThat(result.products()).hasSize(1);
        assertThat(result.products().getFirst().getSelectedOptions()).isNull();
        verify(findProductOptionCategoryPort).findActiveWithOptionsByProductId(90L);
    }

    @Test
    @DisplayName("상품 목록 조회 중 상품이 없으면 예외를 던진다")
    void getProducts_productNotFound_throws() {
        Product product = buildProduct(100L, false);
        PricePolicy policy = buildPricePolicy(1001L, product, null, List.of());

        when(findPricePoliciesPort.findPricePolicies(
                any(), any(), any(), any(), any(), any()
        )).thenReturn(List.of(policy));
        when(findProductPort.findById(100L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> getProductService.getProducts(
                null,
                null,
                10L,
                1,
                Sort.Direction.DESC,
                ProductSortProperty.POPULARITY,
                ProductSearchTarget.NAME,
                null
        ))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("상품을 찾을 수 없습니다");

        verifyNoInteractions(findProductOptionCategoryPort, findProductImagesPort, getProductInventoryUseCase);
    }

    @Test
    @DisplayName("캐시에서 복원된 가격 정책도 productId가 유지되어 정상 조회된다")
    void getProducts_cachedPricePolicyWithProductId_worksCorrectly() {
        stubProductImageExecutor();
        Product product = buildProduct(10L, false);
        PricePolicy cachedPolicy = buildPricePolicyWithProductIdOnly(100L, 10L);

        when(findPricePoliciesPort.findPricePolicies(
                any(), any(), any(), any(), any(), any()
        )).thenReturn(List.of(cachedPolicy));
        when(findProductPort.findById(10L)).thenReturn(Optional.of(product));
        when(getProductInventoryUseCase.getProductStocks(List.of(100L)))
                .thenReturn(Map.of(100L, 5));
        when(findProductImagesPort.findImagesByProductIdAndSort(anyLong(), eq(FileSort.PRODUCT_CATALOG_IMAGE)))
                .thenReturn(Optional.empty());
        when(findProductReviewAggregatesPort.findByProductIds(List.of(10L)))
                .thenReturn(Map.of());
        when(findPricePoliciesPort.countActivePricePoliciesByCategoryId(
                isNull(), eq(ProductSearchTarget.NAME), isNull()
        )).thenReturn(1L);

        GetProductsResult result = getProductService.getProducts(
                null, null, null, 10,
                Sort.Direction.DESC, ProductSortProperty.POPULARITY,
                ProductSearchTarget.NAME, null
        );

        assertThat(result.products()).hasSize(1);
        assertThat(result.products().getFirst().getId()).isEqualTo(10L);
        assertThat(result.products().getFirst().getStock()).isEqualTo(5);
    }

    @Test
    @DisplayName("캐시 적립률 높은순 정렬로 첫 페이지 상품 목록을 조회한다")
    void getProducts_accumulatedPointRateDescFirstPage_includesTotalElements() {
        stubProductImageExecutor();
        Product product1 = buildProduct(120L, false);
        Product product2 = buildProduct(130L, false);
        Product product3 = buildProduct(140L, false);

        PricePolicy policy1 = buildPricePolicy(1201L, product1, null, List.of());
        PricePolicy policy2 = buildPricePolicy(1301L, product2, null, List.of());
        PricePolicy policy3 = buildPricePolicy(1401L, product3, null, List.of());

        when(findPricePoliciesPort.findPricePolicies(
                any(), any(), any(), any(), any(), any()
        )).thenReturn(List.of(policy1, policy2, policy3));

        when(findProductPort.findById(120L)).thenReturn(Optional.of(product1));
        when(findProductPort.findById(130L)).thenReturn(Optional.of(product2));

        when(getProductInventoryUseCase.getProductStocks(List.of(1201L, 1301L)))
                .thenReturn(Map.of(1201L, 5, 1301L, 3));

        when(findProductImagesPort.findImagesByProductIdAndSort(anyLong(), eq(FileSort.PRODUCT_CATALOG_IMAGE)))
                .thenReturn(Optional.empty());

        when(findProductReviewAggregatesPort.findByProductIds(List.of(120L, 130L)))
                .thenReturn(Map.of());

        when(findPricePoliciesPort.countActivePricePoliciesByCategoryId(
                isNull(), eq(ProductSearchTarget.NAME), isNull()
        )).thenReturn(50L);

        GetProductsResult result = getProductService.getProducts(
                null,
                null,
                null,
                2,
                Sort.Direction.DESC,
                ProductSortProperty.ACCUMULATED_POINT_RATE,
                ProductSearchTarget.NAME,
                null
        );

        assertThat(result.totalElements()).isEqualTo(50L);
        assertThat(result.hasNext()).isTrue();
        assertThat(result.nextCursor()).isEqualTo(1301L);
        assertThat(result.products()).hasSize(2);

        verify(findPricePoliciesPort).findPricePolicies(
                isNull(), isNull(), any(), eq(ProductSortProperty.ACCUMULATED_POINT_RATE), any(), isNull()
        );
    }

    @Test
    @DisplayName("캐시 적립률 낮은순 정렬로 상품 목록을 조회한다")
    void getProducts_accumulatedPointRateAsc_callsFindPricePolicies() {
        stubProductImageExecutor();
        Product product = buildProduct(150L, false);
        PricePolicy policy = buildPricePolicy(1501L, product, null, List.of());

        when(findPricePoliciesPort.findPricePolicies(
                any(), any(), any(), any(), any(), any()
        )).thenReturn(List.of(policy));

        when(findProductPort.findById(150L)).thenReturn(Optional.of(product));

        when(getProductInventoryUseCase.getProductStocks(List.of(1501L)))
                .thenReturn(Map.of(1501L, 2));

        when(findProductImagesPort.findImagesByProductIdAndSort(anyLong(), eq(FileSort.PRODUCT_CATALOG_IMAGE)))
                .thenReturn(Optional.empty());

        when(findPricePoliciesPort.countActivePricePoliciesByCategoryId(
                isNull(), eq(ProductSearchTarget.NAME), isNull()
        )).thenReturn(1L);

        GetProductsResult result = getProductService.getProducts(
                null,
                null,
                null,
                10,
                Sort.Direction.ASC,
                ProductSortProperty.ACCUMULATED_POINT_RATE,
                ProductSearchTarget.NAME,
                null
        );

        assertThat(result.products()).hasSize(1);
        assertThat(result.hasNext()).isFalse();

        verify(findPricePoliciesPort).findPricePolicies(
                isNull(), isNull(), any(), eq(ProductSortProperty.ACCUMULATED_POINT_RATE), any(), isNull()
        );
    }

    @Test
    @DisplayName("카테고리별 캐시 적립률 높은순 정렬로 상품 목록을 조회한다")
    void getProducts_accumulatedPointRateDescWithCategory_callsFindByCategoryId() {
        stubProductImageExecutor();
        Long categoryId = 8L;
        Product product = buildProduct(160L, false);
        PricePolicy policy = buildPricePolicy(1601L, product, null, List.of());

        when(findPricePoliciesPort.findPricePoliciesByCategoryId(
                eq(categoryId), any(), any(), any(), any(), any(), any()
        )).thenReturn(List.of(policy));

        when(findProductPort.findById(160L)).thenReturn(Optional.of(product));

        when(getProductInventoryUseCase.getProductStocks(List.of(1601L)))
                .thenReturn(Map.of(1601L, 10));

        when(findProductImagesPort.findImagesByProductIdAndSort(anyLong(), eq(FileSort.PRODUCT_CATALOG_IMAGE)))
                .thenReturn(Optional.empty());

        when(findPricePoliciesPort.countActivePricePoliciesByCategoryId(
                eq(categoryId), eq(ProductSearchTarget.NAME), isNull()
        )).thenReturn(15L);

        GetProductsResult result = getProductService.getProducts(
                categoryId,
                null,
                null,
                10,
                Sort.Direction.DESC,
                ProductSortProperty.ACCUMULATED_POINT_RATE,
                ProductSearchTarget.NAME,
                null
        );

        assertThat(result.totalElements()).isEqualTo(15L);
        assertThat(result.products()).hasSize(1);

        verify(findPricePoliciesPort).findPricePoliciesByCategoryId(
                eq(categoryId), isNull(), isNull(), any(), eq(ProductSortProperty.ACCUMULATED_POINT_RATE), any(), isNull()
        );
        verify(findPricePoliciesPort, never()).findPricePolicies(any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("캐시 적립률 높은순 정렬로 다음 페이지 상품 목록을 조회한다")
    void getProducts_accumulatedPointRateDescWithCursor_skipsTotalElements() {
        stubProductImageExecutor();
        Product product = buildProduct(170L, false);
        PricePolicy policy = buildPricePolicy(1701L, product, null, List.of());

        when(findPricePoliciesPort.findPricePolicies(
                any(), any(), any(), any(), any(), any()
        )).thenReturn(List.of(policy));

        when(findProductPort.findById(170L)).thenReturn(Optional.of(product));

        when(getProductInventoryUseCase.getProductStocks(List.of(1701L)))
                .thenReturn(Map.of(1701L, 7));

        when(findProductImagesPort.findImagesByProductIdAndSort(anyLong(), eq(FileSort.PRODUCT_CATALOG_IMAGE)))
                .thenReturn(Optional.empty());

        GetProductsResult result = getProductService.getProducts(
                null,
                null,
                500L,
                10,
                Sort.Direction.DESC,
                ProductSortProperty.ACCUMULATED_POINT_RATE,
                ProductSearchTarget.NAME,
                null
        );

        assertThat(result.totalElements()).isNull();
        assertThat(result.hasNext()).isFalse();
        assertThat(result.nextCursor()).isEqualTo(1701L);
        assertThat(result.products()).hasSize(1);

        verify(findPricePoliciesPort, never()).countActivePricePoliciesByCategoryId(any(), any(), any());
    }

    @Test
    @DisplayName("리뷰 집계 값이 없으면 평점과 리뷰 수를 0으로 반환한다")
    void getProducts_reviewAggregateNullValues_returnsZero() {
        stubProductImageExecutor();
        Product product = buildProduct(110L, false);
        PricePolicy policy = buildPricePolicy(1101L, product, null, List.of());

        when(findPricePoliciesPort.findPricePolicies(
                any(), any(), any(), any(), any(), any()
        )).thenReturn(List.of(policy));
        when(findProductPort.findById(110L)).thenReturn(Optional.of(product));
        when(getProductInventoryUseCase.getProductStocks(List.of(1101L)))
                .thenReturn(Map.of(1101L, 5));
        when(findProductImagesPort.findImagesByProductIdAndSort(anyLong(), eq(FileSort.PRODUCT_CATALOG_IMAGE)))
                .thenReturn(Optional.empty());
        when(findProductReviewAggregatesPort.findByProductIds(List.of(110L)))
                .thenReturn(Map.of(110L, new ProductReviewAggregateResult(110L, null, null)));

        GetProductsResult result = getProductService.getProducts(
                null,
                null,
                10L,
                1,
                Sort.Direction.DESC,
                ProductSortProperty.POPULARITY,
                ProductSearchTarget.NAME,
                null
        );

        assertThat(result.products()).hasSize(1);
        assertThat(result.products().getFirst().getAverageRating()).isEqualTo(0f);
        assertThat(result.products().getFirst().getTotalCount()).isEqualTo(0);
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
                        .discountRate(BigDecimal.valueOf(10))
                        .accumulatedPoint(100L)
                        .accumulationRate(BigDecimal.valueOf(1))
                        .status(EntityStatus.ACTIVE)
                        .optionIds(optionIds)
                        .productOptions(productOptions)
                        .build()
        );
    }

    private PricePolicy buildPricePolicyWithProductIdOnly(Long id, Long productId) {
        return PricePolicy.from(
                PricePolicySnapshotState.builder()
                        .id(id)
                        .productId(productId)
                        .product(null)
                        .price(10000L)
                        .discountPrice(9000L)
                        .discountRate(BigDecimal.valueOf(10))
                        .accumulatedPoint(100L)
                        .accumulationRate(BigDecimal.valueOf(1))
                        .status(EntityStatus.ACTIVE)
                        .build()
        );
    }

    private GetFilesResult buildFilesResult(Long id, String name) {
        return new GetFilesResult(
                List.of(
                        new GetFileResult(
                                id,
                                FileSort.PRODUCT_CATALOG_IMAGE.name(),
                                "jpg",
                                name,
                                "https://example.com/" + name,
                                List.of(),
                                1L
                        )
                )
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
        when(findProductImagesPort.findImagesByProductIdAndSort(productId, FileSort.PRODUCT_REPRESENTATIVE_IMAGE))
                .thenReturn(Optional.empty());
        when(findProductImagesPort.findImagesByProductIdAndSort(productId, FileSort.PRODUCT_CONTENT_IMAGE))
                .thenReturn(Optional.empty());
    }
}
