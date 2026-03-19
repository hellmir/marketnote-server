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
import com.personal.marketnote.product.port.in.result.product.ProductItemResult;
import com.personal.marketnote.product.port.in.usecase.pricepolicy.GetPricePoliciesUseCase;
import com.personal.marketnote.product.port.in.usecase.product.GetProductInventoryUseCase;
import com.personal.marketnote.product.port.in.usecase.product.GetProductUseCase;
import com.personal.marketnote.product.port.out.file.FindProductImagesPort;
import com.personal.marketnote.product.port.out.pricepolicy.FindPricePoliciesPort;
import com.personal.marketnote.product.port.out.product.FindProductPort;
import com.personal.marketnote.product.port.out.productoption.FindProductOptionCategoryPort;
import com.personal.marketnote.product.port.out.result.ProductReviewAggregateResult;
import com.personal.marketnote.product.port.out.review.FindProductReviewAggregatesPort;
import com.personal.marketnote.product.port.out.shipping.FindShippingPolicyPort;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import static com.personal.marketnote.common.domain.file.FileSort.*;
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
    private final FindProductReviewAggregatesPort findProductReviewAggregatesPort;
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
        targetPricePolicy.setOptionIds();

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
        boolean isFirstPage = FormatValidator.hasNoValue(cursor);

        Pageable pageable = PageRequest.of(
                0, pageSize + 1, Sort.by(sortDirection, sortProperty.getCamelCaseValue())
        );

        boolean isCategorized = FormatValidator.hasValue(categoryId);

        List<PricePolicy> pricePolicies = isCategorized
                ? findPricePoliciesPort.findPricePoliciesByCategoryId(
                categoryId,
                pricePolicyIds,
                cursor,
                pageable,
                sortProperty,
                searchTarget,
                searchKeyword
        )
                : findPricePoliciesPort.findPricePolicies(
                pricePolicyIds,
                cursor,
                pageable,
                sortProperty,
                searchTarget,
                searchKeyword
        );

        // 무한 스크롤 페이지 설정
        boolean hasNext = pricePolicies.size() > pageSize;
        List<PricePolicy> pagedPolicies = hasNext
                ? pricePolicies.subList(0, pageSize)
                : pricePolicies;

        Long nextCursor = null;
        if (FormatValidator.hasValue(pagedPolicies)) {
            nextCursor = pagedPolicies.getLast().getId();
        }

        List<ProductItemResult> productItems = pagedPolicies.stream()
                .map(this::toProductItem)
                .toList();

        Map<Long, ProductReviewAggregateResult> reviewAggregatesByProductId
                = findReviewAggregatesByProductId(productItems);

        // 상품 카탈로그 이미지 병렬 조회
        Map<Long, GetFilesResult> productIdToImages = new ConcurrentHashMap<>();
        List<CompletableFuture<Void>> futures = productItems.stream()
                .map(
                        item -> CompletableFuture.runAsync(
                                () -> findProductImagesPort.findImagesByProductIdAndSort(
                                                item.getId(), PRODUCT_CATALOG_IMAGE
                                        )
                                        .ifPresent(result -> productIdToImages.put(item.getId(), result))
                                , productImageExecutor
                        )
                )
                .toList();
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        // 상품 재고 수량 조회(pricePolicyIds를 통한 요청인 경우 제외)
        Map<Long, Integer> inventories = FormatValidator.hasValue(pricePolicyIds)
                ? Map.of()
                : getProductInventoryUseCase.getProductStocks(
                pagedPolicies.stream()
                        .map(PricePolicy::getId)
                        .toList()
        );

        List<ProductItemResult> productItemsWithImage = productItems.stream()
                .map(item -> ProductItemResult.from(
                        item,
                        FormatValidator.hasValue(productIdToImages.get(item.getId()))
                                ? productIdToImages.get(item.getId())
                                .images()
                                .getFirst()
                                : null,
                        inventories.get(item.getPricePolicyId()),
                        getAverageRating(reviewAggregatesByProductId, item.getId()),
                        getTotalCount(reviewAggregatesByProductId, item.getId())
                ))
                .toList();

        Long totalElements = null;
        if (isFirstPage) {
            totalElements = computeTotalElements(categoryId, searchTarget, searchKeyword);
        }

        return GetProductsResult.from(hasNext, nextCursor, totalElements, productItemsWithImage);
    }

    private Map<Long, ProductReviewAggregateResult> findReviewAggregatesByProductId(
            List<ProductItemResult> productItems
    ) {
        if (FormatValidator.hasNoValue(productItems)) {
            return Map.of();
        }

        List<Long> productIds = productItems.stream()
                .map(ProductItemResult::getId)
                .filter(FormatValidator::hasValue)
                .distinct()
                .toList();

        if (FormatValidator.hasNoValue(productIds)) {
            return Map.of();
        }

        return findProductReviewAggregatesPort.findByProductIds(productIds);
    }

    private Float getAverageRating(
            Map<Long, ProductReviewAggregateResult> reviewAggregatesByProductId,
            Long productId
    ) {
        if (FormatValidator.hasNoValue(reviewAggregatesByProductId)
                || FormatValidator.hasNoValue(productId)) {
            return 0f;
        }

        ProductReviewAggregateResult result = reviewAggregatesByProductId.get(productId);
        if (FormatValidator.hasNoValue(result) || FormatValidator.hasNoValue(result.averageRating())) {
            return 0f;
        }

        return result.averageRating();
    }

    private Integer getTotalCount(
            Map<Long, ProductReviewAggregateResult> reviewAggregatesByProductId,
            Long productId
    ) {
        if (FormatValidator.hasNoValue(reviewAggregatesByProductId)
                || FormatValidator.hasNoValue(productId)) {
            return 0;
        }

        ProductReviewAggregateResult result = reviewAggregatesByProductId.get(productId);
        if (FormatValidator.hasNoValue(result) || FormatValidator.hasNoValue(result.totalCount())) {
            return 0;
        }

        return result.totalCount();
    }

    private ProductItemResult toProductItem(PricePolicy pricePolicy) {
        Product product = getProduct(pricePolicy.getProductId());

        // 옵션을 모두 개별 상품으로 노출하지 않는 경우: 옵션 없이 단일 상품
        if (!product.isFindAllOptionsYn()) {
            return ProductItemResult.from(product, pricePolicy);
        }

        List<Long> optionIds = pricePolicy.getOptionIds();
        if (FormatValidator.hasNoValue(optionIds)) {
            return ProductItemResult.from(product, pricePolicy);
        }

        // 해당 상품의 옵션 카테고리 + 옵션 전체 조회
        List<ProductOptionCategory> categories =
                findProductOptionCategoryPort.findActiveWithOptionsByProductId(product.getId());

        if (FormatValidator.hasNoValue(categories)) {
            return ProductItemResult.from(product, pricePolicy);
        }

        Map<Long, ProductOption> productOptionsById = categories.stream()
                .flatMap(c -> c.getOptions().stream())
                .collect(Collectors.toMap(ProductOption::getId, o -> o, (o1, o2) -> o1));

        List<ProductOption> selectedOptions = optionIds.stream()
                .map(productOptionsById::get)
                .filter(Objects::nonNull)
                .toList();

        return ProductItemResult.from(product, selectedOptions, pricePolicy);
    }

    private Long computeTotalElements(Long categoryId, ProductSearchTarget searchTarget, String searchKeyword) {
        return findPricePoliciesPort.countActivePricePoliciesByCategoryId(categoryId, searchTarget, searchKeyword);
    }
}
