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
import com.personal.marketnote.product.exception.ProductNotFoundException;
import com.personal.marketnote.product.port.in.result.product.GetAdminProductsResult;
import com.personal.marketnote.product.port.in.result.product.ProductItemResult;
import com.personal.marketnote.product.port.in.usecase.product.GetAdminProductsUseCase;
import com.personal.marketnote.product.port.in.usecase.product.GetProductInventoryUseCase;
import com.personal.marketnote.product.port.out.file.FindProductImagesPort;
import com.personal.marketnote.product.port.out.pricepolicy.FindPricePoliciesPort;
import com.personal.marketnote.product.port.out.product.FindProductPort;
import com.personal.marketnote.product.port.out.productoption.FindProductOptionCategoryPort;
import com.personal.marketnote.product.port.out.result.ProductReviewAggregateResult;
import com.personal.marketnote.product.port.out.review.FindProductReviewAggregatesPort;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import static com.personal.marketnote.common.domain.file.FileSort.PRODUCT_CATALOG_IMAGE;
import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true)
public class GetAdminProductsService implements GetAdminProductsUseCase {
    private final FindPricePoliciesPort findPricePoliciesPort;
    private final FindProductPort findProductPort;
    private final FindProductImagesPort findProductImagesPort;
    private final FindProductReviewAggregatesPort findProductReviewAggregatesPort;
    private final GetProductInventoryUseCase getProductInventoryUseCase;
    private final FindProductOptionCategoryPort findProductOptionCategoryPort;

    @Qualifier("productImageExecutor")
    private final Executor productImageExecutor;

    @Override
    public GetAdminProductsResult getAdminProducts(
            Long categoryId,
            List<Long> pricePolicyIds,
            int page,
            int pageSize,
            Sort.Direction sortDirection,
            ProductSortProperty sortProperty,
            ProductSearchTarget searchTarget,
            String searchKeyword
    ) {
        List<PricePolicy> pricePolicies = findPricePoliciesPort.findPricePoliciesByOffset(
                pricePolicyIds, page, pageSize, sortDirection,
                sortProperty, searchTarget, searchKeyword, categoryId
        );

        long totalElements = findPricePoliciesPort.countActivePricePoliciesByCategoryId(
                categoryId, searchTarget, searchKeyword
        );

        int totalPages = computeTotalPages(totalElements, pageSize);

        if (FormatValidator.hasNoValue(pricePolicies)) {
            return GetAdminProductsResult.of(page, pageSize, totalElements, totalPages, List.of());
        }

        List<ProductItemResult> productItems = pricePolicies.stream()
                .map(this::toProductItem)
                .toList();

        Map<Long, ProductReviewAggregateResult> reviewAggregatesByProductId
                = findReviewAggregatesByProductId(productItems);

        Map<Long, GetFilesResult> productIdToImages = findCatalogImages(productItems);

        Map<Long, Integer> inventories = getProductInventoryUseCase.getProductStocks(
                pricePolicies.stream()
                        .map(PricePolicy::getId)
                        .toList()
        );

        List<ProductItemResult> enrichedProducts = productItems.stream()
                .map(item -> ProductItemResult.from(
                        item,
                        extractFirstImage(productIdToImages.get(item.getId())),
                        inventories.get(item.getPricePolicyId()),
                        getAverageRating(reviewAggregatesByProductId, item.getId()),
                        getTotalCount(reviewAggregatesByProductId, item.getId())
                ))
                .toList();

        return GetAdminProductsResult.of(page, pageSize, totalElements, totalPages, enrichedProducts);
    }

    private int computeTotalPages(long totalElements, int pageSize) {
        if (totalElements == 0) {
            return 0;
        }
        return (int) Math.ceil((double) totalElements / pageSize);
    }

    private ProductItemResult toProductItem(PricePolicy pricePolicy) {
        Product product = FormatValidator.hasValue(pricePolicy.getProduct())
                ? pricePolicy.getProduct()
                : findProductPort.findById(pricePolicy.getProductId())
                        .orElseThrow(() -> new ProductNotFoundException(pricePolicy.getProductId()));

        if (!product.isFindAllOptionsYn()) {
            return ProductItemResult.from(product, pricePolicy);
        }

        List<Long> optionIds = pricePolicy.getOptionIds();
        if (FormatValidator.hasNoValue(optionIds)) {
            return ProductItemResult.from(product, pricePolicy);
        }

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

    private Map<Long, GetFilesResult> findCatalogImages(List<ProductItemResult> productItems) {
        Map<Long, GetFilesResult> productIdToImages = new ConcurrentHashMap<>();
        List<CompletableFuture<Void>> futures = productItems.stream()
                .map(item -> CompletableFuture.runAsync(
                        () -> findProductImagesPort.findImagesByProductIdAndSort(
                                        item.getId(), PRODUCT_CATALOG_IMAGE
                                )
                                .ifPresent(result -> productIdToImages.put(item.getId(), result)),
                        productImageExecutor
                ))
                .toList();
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        return productIdToImages;
    }

    private com.personal.marketnote.common.application.file.port.in.result.GetFileResult extractFirstImage(
            GetFilesResult filesResult
    ) {
        if (FormatValidator.hasNoValue(filesResult) || FormatValidator.hasNoValue(filesResult.images())) {
            return null;
        }
        return filesResult.images().getFirst();
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
}
