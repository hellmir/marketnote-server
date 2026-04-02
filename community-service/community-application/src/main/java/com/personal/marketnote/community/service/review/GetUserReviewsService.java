package com.personal.marketnote.community.service.review;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.application.file.port.in.result.GetFileResult;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.community.domain.review.Review;
import com.personal.marketnote.community.domain.review.Reviews;
import com.personal.marketnote.community.port.in.command.review.GetUserReviewsCommand;
import com.personal.marketnote.community.port.in.result.review.GetUserReviewsResult;
import com.personal.marketnote.community.port.in.result.review.UserReviewItemResult;
import com.personal.marketnote.community.port.in.result.review.ReviewProductInfoResult;
import com.personal.marketnote.community.port.in.usecase.review.GetUserReviewsUseCase;
import com.personal.marketnote.community.port.out.file.FindReviewImagesPort;
import com.personal.marketnote.community.port.out.product.FindProductByPricePolicyPort;
import com.personal.marketnote.community.port.out.result.product.ProductInfoResult;
import com.personal.marketnote.community.port.out.review.FindReviewPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import static com.personal.marketnote.common.domain.file.FileSort.REVIEW_IMAGE;
import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true)
public class GetUserReviewsService implements GetUserReviewsUseCase {
    private final FindReviewPort findReviewPort;
    private final FindReviewImagesPort findReviewImagesPort;
    private final FindProductByPricePolicyPort findProductByPricePolicyPort;

    @Override
    public GetUserReviewsResult getUserReviews(GetUserReviewsCommand command) {
        boolean isDesc = Sort.Direction.DESC.equals(command.sortDirection());

        Reviews reviews = findReviewPort.findUserReviewsByOffset(
                command.userId(),
                command.page(),
                command.pageSize(),
                isDesc,
                command.sortProperty()
        );

        long totalElements = findReviewPort.countActive(command.userId());

        int totalPages = computeTotalPages(totalElements, command.pageSize());

        List<Review> reviewList = reviews.getReviews();

        Map<Long, List<GetFileResult>> reviewImagesByReviewId = findReviewImages(reviewList);

        Map<Long, ReviewProductInfoResult> productInfoByPricePolicyId = findReviewProductInfo(reviewList);

        List<UserReviewItemResult> reviewItems = reviewList.stream()
                .map(review -> {
                    Long pricePolicyId = review.getPricePolicyId();
                    ReviewProductInfoResult productInfo = FormatValidator.hasValue(pricePolicyId)
                            ? productInfoByPricePolicyId.get(pricePolicyId)
                            : null;

                    return UserReviewItemResult.from(
                            review,
                            reviewImagesByReviewId.get(review.getId()),
                            productInfo
                    );
                })
                .toList();

        return GetUserReviewsResult.of(
                command.page(),
                command.pageSize(),
                totalElements,
                totalPages,
                reviewItems
        );
    }

    private int computeTotalPages(long totalElements, int pageSize) {
        if (totalElements == 0) {
            return 0;
        }
        return (int) Math.ceil((double) totalElements / pageSize);
    }

    private Map<Long, List<GetFileResult>> findReviewImages(List<Review> reviews) {
        if (FormatValidator.hasNoValue(reviews)) {
            return Map.of();
        }

        Map<Long, List<GetFileResult>> reviewImagesByReviewId = new ConcurrentHashMap<>();

        List<CompletableFuture<Void>> futures = reviews.stream()
                .filter(review -> Boolean.TRUE.equals(review.getIsPhoto()))
                .map(review -> CompletableFuture.runAsync(
                        () -> findReviewImagesPort.findImagesByReviewIdAndSort(review.getId(), REVIEW_IMAGE)
                                .ifPresent(result -> reviewImagesByReviewId.put(review.getId(), result.images()))
                ))
                .toList();

        if (FormatValidator.hasValue(futures)) {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        }

        return reviewImagesByReviewId;
    }

    private Map<Long, ReviewProductInfoResult> findReviewProductInfo(List<Review> reviews) {
        List<Long> pricePolicyIds = extractPricePolicyIds(reviews);
        if (FormatValidator.hasNoValue(pricePolicyIds)) {
            return Map.of();
        }

        Map<Long, ProductInfoResult> productInfoByPricePolicyId
                = findProductByPricePolicyPort.findByPricePolicyIds(pricePolicyIds);
        if (FormatValidator.hasNoValue(productInfoByPricePolicyId)) {
            return Map.of();
        }

        Map<Long, ReviewProductInfoResult> results = new HashMap<>();
        productInfoByPricePolicyId.forEach(
                (pricePolicyId, productInfo) -> results.put(
                        pricePolicyId,
                        ReviewProductInfoResult.from(productInfo)
                )
        );

        return results;
    }

    private List<Long> extractPricePolicyIds(List<Review> reviews) {
        if (FormatValidator.hasNoValue(reviews)) {
            return List.of();
        }

        return reviews.stream()
                .map(Review::getPricePolicyId)
                .filter(FormatValidator::hasValue)
                .distinct()
                .toList();
    }
}
