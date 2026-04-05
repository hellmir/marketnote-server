package com.personal.marketnote.community.service.review;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.application.file.port.in.result.GetFileResult;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.community.domain.like.LikeTargetType;
import com.personal.marketnote.community.domain.review.ProductReviewAggregate;
import com.personal.marketnote.community.domain.review.Review;
import com.personal.marketnote.community.domain.review.ReviewSortProperty;
import com.personal.marketnote.community.domain.review.Reviews;
import com.personal.marketnote.community.exception.NotReviewAuthorException;
import com.personal.marketnote.community.exception.ProductReviewAggregateNotFoundException;
import com.personal.marketnote.community.exception.ReviewAlreadyExistsException;
import com.personal.marketnote.community.exception.ReviewNotFoundException;
import com.personal.marketnote.community.port.in.command.review.RegisterReviewCommand;
import com.personal.marketnote.community.port.in.result.review.*;
import com.personal.marketnote.community.port.in.usecase.like.GetLikeUseCase;
import com.personal.marketnote.community.port.in.usecase.review.GetReviewUseCase;
import com.personal.marketnote.community.port.out.file.FindReviewImagesPort;
import com.personal.marketnote.community.port.out.order.FindOrderProductPort;
import com.personal.marketnote.community.port.out.product.FindProductByPricePolicyPort;
import com.personal.marketnote.community.port.out.result.product.ProductInfoResult;
import com.personal.marketnote.community.port.out.review.FindReviewPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import static com.personal.marketnote.common.domain.file.FileSort.REVIEW_IMAGE;
import static com.personal.marketnote.common.utility.ApiConstant.FIRST_PAGE_CURSOR_VALUE;
import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;
import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true)
public class GetReviewService implements GetReviewUseCase {
    private final FindReviewPort findReviewPort;
    private final FindReviewImagesPort findReviewImagesPort;
    private final FindProductByPricePolicyPort findProductByPricePolicyPort;
    private final FindOrderProductPort findOrderProductPort;
    private final GetLikeUseCase getLikeUseCase;

    @Override
    public Review getReview(Long id) {
        return findReviewPort.findById(id)
                .orElseThrow(() -> new ReviewNotFoundException(id));
    }

    @Override
    public Review getReview(Long id, Long userId) {
        Review review = findReviewPort.findById(id)
                .orElseThrow(() -> new ReviewNotFoundException(id));
        review.updateIsUserLiked(
                getLikeUseCase.existsUserLike(LikeTargetType.REVIEW, review.getId(), userId)
        );

        return review;
    }

    @Override
    public void validateDuplicateReview(RegisterReviewCommand command) {
        Long orderId = command.orderId();
        Long pricePolicyId = command.pricePolicyId();

        if (findReviewPort.existsByOrderIdAndPricePolicyId(orderId, pricePolicyId)) {
            throw new ReviewAlreadyExistsException(orderId, pricePolicyId);
        }
    }

    @Override
    public GetReviewsResult getProductReviews(
            Long userId,
            Long productId,
            Boolean isPhoto,
            Long cursor,
            int pageSize,
            Sort.Direction sortDirection,
            ReviewSortProperty sortProperty
    ) {
        Pageable pageable = PageRequest.of(
                0, pageSize + 1, Sort.by(sortDirection, sortProperty.getCamelCaseValue())
        );

        Reviews productReviews = findReviewPort.findProductReviews(
                productId, isPhoto, cursor, pageable, sortProperty
        );

        // 무한 스크롤 페이지 설정
        boolean hasNext = productReviews.size() > pageSize;
        List<Review> pagedReviews = hasNext
                ? productReviews.subList(0, pageSize)
                : productReviews.getReviews();

        Long nextCursor = null;
        if (FormatValidator.hasValue(pagedReviews)) {
            nextCursor = pagedReviews.getLast().getId();
        }

        boolean isFirstPage = FormatValidator.equals(cursor, FIRST_PAGE_CURSOR_VALUE);
        Long totalElements = null;
        if (isFirstPage) {
            totalElements = findReviewPort.countActive(productId, isPhoto);
        }

        Map<Long, List<GetFileResult>> reviewImagesByReviewId = findReviewImages(pagedReviews);

        if (!isPhoto && FormatValidator.hasValue(userId)) {
            // 로그인 사용자가 각 리뷰에 좋아요를 눌렀는지 여부 업데이트
            pagedReviews.forEach(review -> review.updateIsUserLiked(
                    getLikeUseCase.existsUserLike(LikeTargetType.REVIEW, review.getId(), userId)
            ));
        }

        return GetReviewsResult.from(hasNext, nextCursor, totalElements, pagedReviews, reviewImagesByReviewId);
    }

    @Override
    public boolean isReviewCountUnderHundred(Long productId) {
        return findReviewPort.countActive(productId, false) < 100;
    }

    @Override
    @Transactional(isolation = READ_COMMITTED, readOnly = true, propagation = REQUIRES_NEW)
    public ProductReviewAggregate getProductReviewAggregate(Long productId) {
        return findReviewPort.findProductReviewAggregateByProductId(productId)
                .orElseThrow(() -> new ProductReviewAggregateNotFoundException(productId));
    }

    @Override
    public GetProductReviewAggregatesResult getProductReviewAggregates(List<Long> productIds) {
        if (FormatValidator.hasNoValue(productIds)) {
            return GetProductReviewAggregatesResult.empty();
        }

        List<Long> validProductIds = productIds.stream()
                .filter(productId -> FormatValidator.hasValue(productId))
                .distinct()
                .toList();

        Map<Long, ProductReviewAggregate> aggregatesByProductId = FormatValidator.hasValue(validProductIds)
                ? findReviewPort.findProductReviewAggregatesByProductIds(validProductIds)
                : Map.of();

        List<ProductReviewAggregateSummaryResult> reviewAggregates = productIds.stream()
                .filter(productId -> FormatValidator.hasValue(productId))
                .map(productId -> ProductReviewAggregateSummaryResult.from(
                        productId,
                        aggregatesByProductId.get(productId)
                ))
                .toList();

        return GetProductReviewAggregatesResult.from(reviewAggregates);
    }

    @Override
    public GetMyReviewsResult getWriterReviews(
            Long userId, Long cursor, int pageSize, Sort.Direction sortDirection, ReviewSortProperty sortProperty
    ) {
        Pageable pageable = PageRequest.of(
                0, pageSize + 1, Sort.by(sortDirection, sortProperty.getCamelCaseValue())
        );

        Reviews userReviews = findReviewPort.findUserReviews(
                userId, cursor, pageable, sortProperty
        );

        // 무한 스크롤 페이지 설정
        boolean hasNext = userReviews.size() > pageSize;
        List<Review> pagedReviews = hasNext
                ? userReviews.subList(0, pageSize)
                : userReviews.getReviews();

        Long nextCursor = null;
        if (FormatValidator.hasValue(pagedReviews)) {
            nextCursor = pagedReviews.getLast().getId();
        }

        boolean isFirstPage = FormatValidator.equals(cursor, FIRST_PAGE_CURSOR_VALUE);
        Long totalElements = null;
        if (isFirstPage) {
            totalElements = findReviewPort.countActive(userId);
        }

        Map<Long, List<GetFileResult>> reviewImages = findReviewImages(pagedReviews);

        Map<Long, ReviewProductInfoResult> productInfoByPricePolicyId = findReviewProductInfo(pagedReviews);

        return GetMyReviewsResult.from(
                hasNext,
                nextCursor,
                totalElements,
                pagedReviews,
                reviewImages,
                productInfoByPricePolicyId
        );
    }

    @Override
    public GetReviewCountResult getWriterReviewCount(Long userId) {
        long totalCount = findReviewPort.countActive(userId);

        return GetReviewCountResult.of(totalCount);
    }

    @Override
    public ReviewItemResult getReviewDetail(Long id, Long userId) {
        Review review = findReviewPort.findById(id)
                .orElseThrow(() -> new ReviewNotFoundException(id));

        if (FormatValidator.hasValue(userId)) {
            review.updateIsUserLiked(
                    getLikeUseCase.existsUserLike(LikeTargetType.REVIEW, review.getId(), userId)
            );
        }

        List<GetFileResult> images = findReviewDetailImages(review);
        ReviewProductInfoResult product = findReviewDetailProductInfo(review);

        return ReviewItemResult.from(review, images, product);
    }

    private List<GetFileResult> findReviewDetailImages(Review review) {
        if (!Boolean.TRUE.equals(review.getIsPhoto())) {
            return null;
        }

        return findReviewImagesPort.findImagesByReviewIdAndSort(review.getId(), REVIEW_IMAGE)
                .map(result -> result.images())
                .orElse(null);
    }

    private ReviewProductInfoResult findReviewDetailProductInfo(Review review) {
        Long pricePolicyId = review.getPricePolicyId();
        if (FormatValidator.hasNoValue(pricePolicyId)) {
            return null;
        }

        Map<Long, ProductInfoResult> productInfoMap
                = findProductByPricePolicyPort.findByPricePolicyIds(List.of(pricePolicyId));
        ProductInfoResult productInfo = productInfoMap.get(pricePolicyId);
        if (FormatValidator.hasNoValue(productInfo)) {
            return null;
        }

        Long unitAmount = null;
        if (FormatValidator.hasValue(review.getOrderId())) {
            unitAmount = findOrderProductPort
                    .findUnitAmountByOrderIdAndPricePolicyId(review.getOrderId(), pricePolicyId)
                    .orElse(null);
        }

        return ReviewProductInfoResult.from(productInfo, unitAmount);
    }

    @Override
    public boolean existsReview(Long id) {
        return findReviewPort.existsById(id);
    }

    @Override
    public void validateAuthor(Long id, Long reviewerId) {
        if (!findReviewPort.existsByIdAndReviewerId(id, reviewerId)) {
            throw new NotReviewAuthorException(id, reviewerId);
        }
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
