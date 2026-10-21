package com.personal.marketnote.product.service.product;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.application.file.port.in.result.GetFilesResult;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.product.domain.option.ProductOption;
import com.personal.marketnote.product.domain.option.ProductOptionCategory;
import com.personal.marketnote.product.domain.pricepolicy.PricePolicy;
import com.personal.marketnote.product.domain.product.Product;
import com.personal.marketnote.product.domain.product.ProductSearchTarget;
import com.personal.marketnote.product.domain.product.ProductSortProperty;
import com.personal.marketnote.product.domain.shipping.ShippingPolicy;
import com.personal.marketnote.product.exception.PricePolicyNotFoundException;
import com.personal.marketnote.product.exception.ProductNotFoundException;
import com.personal.marketnote.product.port.in.result.option.SelectableProductOptionCategoryItemResult;
import com.personal.marketnote.product.port.in.result.product.GetProductInfoResult;
import com.personal.marketnote.product.port.in.result.product.GetProductInfoWithOptionsResult;
import com.personal.marketnote.product.port.in.result.product.GetProductsResult;
import com.personal.marketnote.product.port.in.result.product.ProductCatalogItemResult;
import com.personal.marketnote.product.port.in.result.product.ProductItemResult;
import com.personal.marketnote.product.port.in.usecase.pricepolicy.GetPricePoliciesUseCase;
import com.personal.marketnote.product.port.in.usecase.product.GetProductInventoryUseCase;
import com.personal.marketnote.product.port.in.usecase.product.GetProductUseCase;
import com.personal.marketnote.product.port.out.file.FindProductImagesPort;
import com.personal.marketnote.product.port.out.pricepolicy.FindPricePoliciesPort;
import com.personal.marketnote.product.port.out.product.FindProductPort;
import com.personal.marketnote.product.port.out.productcatalog.FindProductCatalogReadModelPort;
import com.personal.marketnote.product.port.out.productoption.FindProductOptionCategoryPort;
import com.personal.marketnote.product.port.out.shipping.FindShippingPolicyPort;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static com.personal.marketnote.common.domain.file.FileSort.*;
import static com.personal.marketnote.common.utility.ApiConstant.FIRST_PAGE_CURSOR_VALUE;
import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true)
public class GetProductService implements GetProductUseCase {
    private final GetPricePoliciesUseCase getPricePoliciesUseCase;
    private final GetProductInventoryUseCase getProductInventoryUseCase;
    private final FindProductPort findProductPort;
    private final FindProductOptionCategoryPort findProductOptionCategoryPort;
    private final FindPricePoliciesPort findPricePoliciesPort;
    private final FindProductImagesPort findProductImagesPort;
    private final FindProductCatalogReadModelPort findProductCatalogReadModelPort;
    private final FindShippingPolicyPort findShippingPolicyPort;

    @Qualifier("productImageExecutor")
    private final Executor productImageExecutor;

    @Override
    public GetProductInfoWithOptionsResult getProductInfo(Long pricePolicyId) {
        List<PricePolicy> pricePolicies = getPricePoliciesUseCase.getPricePoliciesAndOptions(List.of(pricePolicyId));
        if (FormatValidator.hasNoValue(pricePolicies)) {
            throw new PricePolicyNotFoundException(pricePolicyId);
        }
        PricePolicy targetPricePolicy = pricePolicies.getFirst();
        targetPricePolicy.syncOptionIdsFromOptions();

        return getProductInfo(targetPricePolicy.getProductId(), targetPricePolicy.getOptionIds());
    }

    @Override
    public GetProductInfoWithOptionsResult getProductInfo(Long id, List<Long> selectedOptionIds) {
        Product product = findProductPort.findActiveById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        return buildProductInfo(product, id, selectedOptionIds);
    }

    @Override
    public GetProductInfoWithOptionsResult getProductInfoIncludingInactive(Long id, List<Long> selectedOptionIds) {
        Product product = getProduct(id);
        return buildProductInfo(product, id, selectedOptionIds);
    }

    private GetProductInfoWithOptionsResult buildProductInfo(
            Product product, Long id, List<Long> selectedOptionIds
    ) {
        // 상단 대표 이미지 목록, 본문 이미지 목록 조회 시작
        CompletableFuture<GetFilesResult> representativeFuture =
                CompletableFuture.supplyAsync(
                        () -> findProductImagesPort.findImagesByProductIdAndSort(
                                id, PRODUCT_REPRESENTATIVE_IMAGE
                        ).orElse(null),
                        productImageExecutor
                );

        CompletableFuture<GetFilesResult> contentFuture =
                CompletableFuture.supplyAsync(
                        () -> findProductImagesPort.findImagesByProductIdAndSort(
                                id, PRODUCT_CONTENT_IMAGE
                        ).orElse(null),
                        productImageExecutor
                );

        // 상품 조회 (병렬 진행)
        List<ProductOptionCategory> categories
                = findProductOptionCategoryPort.findActiveWithOptionsByProductId(product.getId());
        List<PricePolicy> pricePolicies
                = findPricePoliciesPort.findByProductId(product.getId());
        PricePolicy defaultPricePolicy = pricePolicies.stream()
                .filter(policy -> !policy.hasOptions())
                .findFirst()
                .orElse(null);
        PricePolicy selectedPricePolicy = defaultPricePolicy;

        if (
                product.isFindAllOptionsYn()
                        && FormatValidator.hasValue(selectedOptionIds)
                        && FormatValidator.hasValue(categories)
        ) {
            selectedPricePolicy = pricePolicies.stream()
                    .filter(policy -> FormatValidator.equals(policy.getOptionIds(), selectedOptionIds))
                    .findFirst()
                    .orElse(defaultPricePolicy);
        }

        if (FormatValidator.hasNoValue(selectedPricePolicy)) {
            throw new PricePolicyNotFoundException(null);
        }

        Long pricePolicyId = selectedPricePolicy.getId();

        // 상품 재고 수량 조회
        Map<Long, Integer> inventories = getProductInventoryUseCase.getProductStocks(List.of(pricePolicyId));

        // 판매자 배송비 정책 조회
        ShippingPolicy shippingPolicy = findShippingPolicyPort.findActiveBySellerId(product.getSellerId())
                .orElse(null);

        GetProductInfoResult productInfo
                = GetProductInfoResult.from(product, selectedPricePolicy, inventories.get(pricePolicyId), shippingPolicy);

        List<SelectableProductOptionCategoryItemResult> selectableCategories
                = categories.stream()
                .map(c -> SelectableProductOptionCategoryItemResult.from(c, selectedOptionIds))
                .toList();

        // 이미지 결과 대기
        GetFilesResult representativeImages = representativeFuture.join();
        GetFilesResult contentImages = contentFuture.join();

        return GetProductInfoWithOptionsResult.of(
                productInfo, selectableCategories, representativeImages, contentImages, pricePolicies
        );
    }

    @Override
    public Product getProduct(Long id) {
        return findProductPort.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    @Override
    public GetProductsResult getProducts(
            Long categoryId,
            List<Long> pricePolicyIds,
            Long cursor,
            int pageSize,
            Sort.Direction sortDirection,
            ProductSortProperty sortProperty,
            ProductSearchTarget searchTarget,
            String searchKeyword
    ) {
        boolean isFirstPage = FormatValidator.equals(cursor, FIRST_PAGE_CURSOR_VALUE);
        boolean isAsc = sortDirection.isAscending();

        List<ProductCatalogItemResult> catalogItems = findProductCatalogReadModelPort.findCatalogItems(
                categoryId,
                pricePolicyIds,
                cursor,
                pageSize + 1,
                isAsc,
                sortProperty,
                searchTarget,
                searchKeyword
        );

        // 무한 스크롤 페이지 설정
        boolean hasNext = catalogItems.size() > pageSize;
        List<ProductCatalogItemResult> pagedCatalogItems = hasNext
                ? catalogItems.subList(0, pageSize)
                : catalogItems;

        Long nextCursor = null;
        if (FormatValidator.hasValue(pagedCatalogItems)) {
            nextCursor = pagedCatalogItems.getLast().pricePolicyId();
        }

        List<ProductItemResult> productItems = pagedCatalogItems.stream()
                .map(ProductItemResult::fromReadModel)
                .toList();

        Long totalElements = null;
        if (isFirstPage) {
            totalElements = findProductCatalogReadModelPort.countCatalogItems(categoryId, searchTarget, searchKeyword);
        }

        return GetProductsResult.from(hasNext, nextCursor, totalElements, productItems);
    }
}
