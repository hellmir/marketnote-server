package com.personal.marketnote.community.port.out.review;

import com.personal.marketnote.community.domain.review.ProductReviewAggregate;
import com.personal.marketnote.community.domain.review.Review;
import com.personal.marketnote.community.domain.review.ReviewSortProperty;
import com.personal.marketnote.community.domain.review.Reviews;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 리뷰 조회 포트
 *
 * @Author 성효빈
 * @Date 2026-01-09
 * @Description 리뷰 조회 관련 기능을 제공합니다.
 */
public interface FindReviewPort {
    /**
     * @param id 리뷰 ID
     * @return 리뷰 존재 여부 {@link boolean}
     * @Date 2026-01-12
     * @Author 성효빈
     * @Description 리뷰 존재 여부를 조회합니다.
     */
    boolean existsById(Long id);

    /**
     * @param id         리뷰 ID
     * @param reviewerId 리뷰 작성자 ID
     * @return 리뷰 작성자 여부 {@link boolean}
     * @Date 2026-01-12
     * @Author 성효빈
     * @Description 리뷰 작성자 여부를 조회합니다.
     */
    boolean existsByIdAndReviewerId(Long id, Long reviewerId);

    /**
     * @param orderId       주문 ID
     * @param pricePolicyId 가격 정책 ID
     * @return 주문 ID와 가격 정책 ID에 해당하는 리뷰 존재 여부 {@link boolean}
     * @Date 2026-01-10
     * @Author 성효빈
     * @Description 주문 ID와 가격 정책 ID에 해당하는 리뷰 존재 여부를 조회합니다.
     */
    boolean existsByOrderIdAndPricePolicyId(Long orderId, Long pricePolicyId);

    /**
     * @param id 리뷰 ID
     * @return 리뷰 {@link Review}
     * @Date 2026-01-12
     * @Author 성효빈
     * @Description 리뷰를 조회합니다.
     */
    Optional<Review> findById(Long id);

    /**
     * @param productId    상품 ID
     * @param isPhoto      포토 리뷰 여부
     * @param cursor       커서(무한 스크롤 페이지 설정)
     * @param pageable     페이지네이션 정보
     * @param sortProperty 정렬 속성
     * @return 상품 리뷰 목록 {@link List<Review>}
     * @Date 2026-01-10
     * @Author 성효빈
     * @Description 상품 리뷰 목록을 조회합니다.
     */
    Reviews findProductReviews(
            Long productId, Boolean isPhoto, Long cursor, Pageable pageable, ReviewSortProperty sortProperty
    );

    /**
     * @param productId 상품 ID
     * @param isPhoto   포토 리뷰 여부
     * @return 총 리뷰 개수 {@link long}
     * @Date 2026-01-10
     * @Author 성효빈
     * @Description 총 리뷰 개수를 조회합니다.
     */
    long countActive(Long productId, Boolean isPhoto);

    /**
     * @param productId 상품 ID
     * @return 상품 리뷰 집계 정보 {@link Optional<ProductReviewAggregate>}
     * @Date 2026-01-10
     * @Author 성효빈
     * @Description 상품 리뷰 집계 정보를 조회합니다.
     */
    Optional<ProductReviewAggregate> findProductReviewAggregateByProductId(Long productId);

    /**
     * @param productIds 상품 ID 목록
     * @return 상품 리뷰 집계 목록 {@link Map<Long, ProductReviewAggregate>}
     * @Date 2026-01-31
     * @Author 성효빈
     * @Description 상품 리뷰 집계 목록을 조회합니다.
     */
    Map<Long, ProductReviewAggregate> findProductReviewAggregatesByProductIds(List<Long> productIds);

    /**
     * @param reviewerId   리뷰 작성자 ID
     * @param cursor       커서(무한 스크롤 페이지 설정)
     * @param pageable     페이지네이션 정보
     * @param sortProperty 정렬 속성
     * @return 회원 리뷰 목록 {@link Reviews}
     * @Date 2026-01-12
     * @Author 성효빈
     * @Description 회원 리뷰 목록을 조회합니다.
     */
    Reviews findUserReviews(Long reviewerId, Long cursor, Pageable pageable, ReviewSortProperty sortProperty);

    /**
     * @param reviewerId 리뷰 작성자 ID
     * @return 총 리뷰 개수 {@link long}
     * @Date 2026-01-12
     * @Author 성효빈
     * @Description 총 리뷰 개수를 조회합니다.
     */
    long countActive(Long reviewerId);

    /**
     * @param reviewerId   리뷰 작성자 ID
     * @param page         페이지 번호 (1부터 시작)
     * @param pageSize     페이지 크기
     * @param isDesc       내림차순 여부
     * @param sortProperty 정렬 속성
     * @return 회원 리뷰 목록 {@link Reviews}
     * @Date 2026-04-01
     * @Author 성효빈
     * @Description 회원 리뷰 목록을 오프셋 기반으로 조회합니다.
     */
    Reviews findUserReviewsByOffset(
            Long reviewerId,
            int page,
            int pageSize,
            boolean isDesc,
            ReviewSortProperty sortProperty
    );
}
