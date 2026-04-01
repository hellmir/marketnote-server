package com.personal.marketnote.community.adapter.in.web.review.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.common.utility.ElementExtractor;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.community.adapter.in.web.review.controller.apidocs.*;
import com.personal.marketnote.community.adapter.in.web.review.mapper.ReviewRequestToCommandMapper;
import com.personal.marketnote.community.adapter.in.web.review.request.RegisterReviewRequest;
import com.personal.marketnote.community.adapter.in.web.review.request.UpdateReviewRequest;
import com.personal.marketnote.community.adapter.in.web.review.response.*;
import com.personal.marketnote.community.domain.review.ReviewSortProperty;
import com.personal.marketnote.community.exception.ProductReviewAggregateNotFoundException;
import com.personal.marketnote.community.port.in.result.review.*;
import com.personal.marketnote.community.port.in.command.review.GetUserReviewsCommand;
import com.personal.marketnote.community.port.in.usecase.review.DeleteReviewUseCase;
import com.personal.marketnote.community.port.in.usecase.review.GetReviewUseCase;
import com.personal.marketnote.community.port.in.usecase.review.GetUserReviewsUseCase;
import com.personal.marketnote.community.port.in.usecase.review.RegisterReviewUseCase;
import com.personal.marketnote.community.port.in.usecase.review.UpdateReviewUseCase;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.personal.marketnote.common.domain.exception.ExceptionCode.DEFAULT_SUCCESS_CODE;
import static com.personal.marketnote.common.utility.ApiConstant.ADMIN_POINTCUT;
import static org.apache.commons.lang3.BooleanUtils.FALSE;

/**
 * 리뷰 컨트롤러
 *
 * @Author 성효빈
 * @Date 2026-01-09
 * @Description 리뷰 관련 API를 제공합니다.
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "리뷰 API", description = "리뷰 관련 API")
@RequiredArgsConstructor
public class ReviewController {
    private static final String GET_PRODUCT_REVIEWS_DEFAULT_PAGE_SIZE = "4";

    private static final String DEFAULT_PAGE_SIZE = "10";

    private final RegisterReviewUseCase registerReviewUseCase;
    private final GetReviewUseCase getReviewUseCase;
    private final GetUserReviewsUseCase getUserReviewsUseCase;
    private final UpdateReviewUseCase updateReviewUseCase;
    private final DeleteReviewUseCase deleteReviewUseCase;

    /**
     * 리뷰 등록
     *
     * @param request   리뷰 등록 요청
     * @param principal 인증된 사용자 정보
     * @return 리뷰 등록 응답 {@link RegisterReviewResponse}
     * @Author 성효빈
     * @Date 2026-01-09
     * @Description 상품 리뷰를 등록합니다.
     */
    @PostMapping("/reviews")
    @RegisterReviewApiDocs
    public ResponseEntity<BaseResponse<RegisterReviewResponse>> registerReview(
            @Valid @RequestBody RegisterReviewRequest request,
            @AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal
    ) {
        RegisterReviewResult result = registerReviewUseCase.registerReview(
                ReviewRequestToCommandMapper.mapToCommand(request, ElementExtractor.extractUserId(principal))
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        RegisterReviewResponse.from(result),
                        HttpStatus.CREATED,
                        DEFAULT_SUCCESS_CODE,
                        "리뷰 등록 성공"
                ),
                HttpStatus.CREATED
        );
    }

    /**
     * 상품 리뷰 목록 조회
     *
     * @param productId     상품 ID
     * @param cursor        커서(무한 스크롤 페이지 설정)
     * @param pageSize      페이지 크기
     * @param sortDirection 정렬 방향
     * @param sortProperty  정렬 속성
     * @return 리뷰 목록 조회 응답 {@link GetReviewsResult}
     * @Author 성효빈
     * @Date 2026-01-10
     * @Description 상품 리뷰 목록을 조회합니다.
     */
    @GetMapping("/products/{productId}/reviews")
    @GetProductReviewsApiDocs
    public ResponseEntity<BaseResponse<GetReviewsResponse>> getProductReviews(
            @PathVariable("productId") Long productId,
            @RequestParam(value = "isPhoto", required = false, defaultValue = FALSE) Boolean isPhoto,
            @RequestParam(value = "cursor", required = false) Long cursor,
            @RequestParam(value = "pageSize", required = false, defaultValue = GET_PRODUCT_REVIEWS_DEFAULT_PAGE_SIZE) int pageSize,
            @RequestParam(required = false, defaultValue = "DESC") Sort.Direction sortDirection,
            @RequestParam(required = false, defaultValue = "ID") ReviewSortProperty sortProperty,
            @AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal
    ) {
        Long userId = null;
        if (FormatValidator.hasValue(principal)) {
            userId = ElementExtractor.extractUserId(principal);
        }

        GetReviewsResult result = getReviewUseCase.getProductReviews(
                userId,
                productId,
                isPhoto,
                cursor,
                pageSize,
                sortDirection,
                sortProperty
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        GetReviewsResponse.from(result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "상품 리뷰 목록 조회 성공"
                ),
                HttpStatus.OK
        );
    }

    /**
     * 나의 리뷰 목록 조회
     *
     * @param cursor        커서(무한 스크롤 페이지 설정)
     * @param pageSize      페이지 크기
     * @param sortDirection 정렬 방향
     * @param sortProperty  정렬 속성
     * @return 리뷰 목록 조회 응답 {@link GetReviewsResult}
     * @Author 성효빈
     * @Date 2026-01-12
     * @Description 나의 리뷰 목록을 조회합니다.
     */
    @GetMapping("/reviews/me")
    @GetMyReviewsApiDocs
    public ResponseEntity<BaseResponse<GetReviewsResponse>> getMyReviews(
            @RequestParam(value = "cursor", required = false) Long cursor,
            @RequestParam(value = "pageSize", required = false, defaultValue = GET_PRODUCT_REVIEWS_DEFAULT_PAGE_SIZE) int pageSize,
            @RequestParam(required = false, defaultValue = "DESC") Sort.Direction sortDirection,
            @RequestParam(required = false, defaultValue = "ID") ReviewSortProperty sortProperty,
            @AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal
    ) {
        GetReviewsResult result = getReviewUseCase.getWriterReviews(
                ElementExtractor.extractUserId(principal),
                cursor,
                pageSize,
                sortDirection,
                sortProperty
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        GetReviewsResponse.from(result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "나의 리뷰 목록 조회 성공"
                ),
                HttpStatus.OK
        );
    }

    /**
     * 리뷰 상세 정보 조회
     *
     * @return 리뷰 상세 정보 조회 응답 {@link ReviewItemResponse}
     * @Author 성효빈
     * @Date 2026-01-27
     * @Description 리뷰 상세 정보를 조회합니다.
     */
    @GetMapping("/reviews/{id}")
    @GetReviewApiDocs
    public ResponseEntity<BaseResponse<ReviewItemResponse>> getReview(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal
    ) {
        ReviewItemResult result = ReviewItemResult.from(
                getReviewUseCase.getReview(id, ElementExtractor.extractUserId(principal))
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        ReviewItemResponse.from(result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "리뷰 상세 정보 조회 성공"
                ),
                HttpStatus.OK
        );
    }

    /**
     * 나의 리뷰 개수 조회
     *
     * @param principal 인증된 사용자 정보
     * @return 나의 리뷰 개수 조회 응답 {@link GetReviewsCountResponse}
     * @Author 성효빈
     * @Date 2026-01-19
     * @Description 나의 리뷰 개수를 조회합니다.
     */
    @GetMapping("/reviews/me/count")
    @GetMyReviewsCountApiDocs
    public ResponseEntity<BaseResponse<GetReviewsCountResponse>> getMyReviewsCount(
            @AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal
    ) {
        GetReviewCountResult result = getReviewUseCase.getWriterReviewCount(
                ElementExtractor.extractUserId(principal)
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        GetReviewsCountResponse.from(result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "나의 리뷰 개수 조회 성공"
                ),
                HttpStatus.OK
        );
    }

    /**
     * 리뷰 수정
     *
     * @param id        리뷰 ID
     * @param request   리뷰 수정 요청
     * @param principal 인증된 사용자 정보
     * @Author 성효빈
     * @Date 2026-01-12
     * @Description 상품 리뷰를 수정합니다.
     */
    @PatchMapping("/reviews/{id}")
    @UpdateReviewApiDocs
    public ResponseEntity<BaseResponse<Void>> updateReview(
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateReviewRequest request,
            @AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal
    ) {
        updateReviewUseCase.updateReview(
                ReviewRequestToCommandMapper.mapToCommand(id, request, ElementExtractor.extractUserId(principal))
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "리뷰 수정 성공"
                ),
                HttpStatus.OK
        );
    }

    /**
     * 리뷰 삭제
     *
     * @param id        리뷰 ID
     * @param principal 인증된 사용자 정보
     * @Author 성효빈
     * @Date 2026-01-12
     * @Description 상품 리뷰를 삭제합니다.
     */
    @DeleteMapping("/reviews/{id}")
    @DeleteReviewApiDocs
    public ResponseEntity<BaseResponse<Void>> deleteReview(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal
    ) {
        deleteReviewUseCase.deleteReview(id, ElementExtractor.extractUserId(principal));

        return new ResponseEntity<>(
                BaseResponse.of(
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "리뷰 삭제 성공"
                ),
                HttpStatus.OK
        );
    }

    /**
     * 상품 리뷰 평점 평균 및 점수별 개수 현황 조회
     *
     * @param productId 상품 ID
     * @return 상품 리뷰 평점 평균 및 점수별 개수 현황 조회 응답 {@link GetProductReviewAggregateResponse}
     * @Author 성효빈
     * @Date 2026-01-10
     * @Description 상품 리뷰 평점 평균 및 점수별 개수 현황을 조회합니다.
     */
    @GetMapping("products/{productId}/review-aggregate")
    @GetProductReviewAggregateApiDocs
    public ResponseEntity<BaseResponse<GetProductReviewAggregateResponse>> getProductReviewAggregate(
            @PathVariable("productId") Long productId
    ) {
        try {
            return new ResponseEntity<>(
                    BaseResponse.of(
                            GetProductReviewAggregateResponse.from(
                                    ProductReviewAggregateResult.from(
                                            getReviewUseCase.getProductReviewAggregate(productId)
                                    )
                            ),
                            HttpStatus.OK,
                            DEFAULT_SUCCESS_CODE,
                            "상품 리뷰 평점 평균 및 점수별 개수 현황 조회 성공"
                    ),
                    HttpStatus.OK
            );
        } catch (ProductReviewAggregateNotFoundException pranfe) {
            return new ResponseEntity<>(
                    BaseResponse.of(
                            GetProductReviewAggregateResponse.from(
                                    ProductReviewAggregateResult.from(null)
                            ),
                            HttpStatus.OK,
                            DEFAULT_SUCCESS_CODE,
                            "상품 리뷰 평점 평균 및 점수별 개수 현황 조회 성공"
                    ),
                    HttpStatus.OK
            );
        }
    }

    /**
     * 상품 리뷰 집계 목록 조회
     *
     * @param productIds 상품 ID 목록
     * @return 상품 리뷰 집계 목록 조회 응답 {@link GetProductReviewAggregatesResponse}
     * @Author 성효빈
     * @Date 2026-01-31
     * @Description 상품 리뷰 평점 평균 및 총 리뷰 개수 목록을 조회합니다.
     */
    @GetMapping("products/review-aggregates")
    @PreAuthorize(ADMIN_POINTCUT)
    @GetProductReviewAggregatesApiDocs
    public ResponseEntity<BaseResponse<GetProductReviewAggregatesResponse>> getProductReviewAggregates(
            @RequestParam("productIds") List<Long> productIds
    ) {
        return new ResponseEntity<>(
                BaseResponse.of(
                        GetProductReviewAggregatesResponse.from(
                                getReviewUseCase.getProductReviewAggregates(productIds)
                        ),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "상품 리뷰 집계 목록 조회 성공"
                ),
                HttpStatus.OK
        );
    }

    /**
     * (관리자) 회원 리뷰 내역 조회
     *
     * @param userId        회원 ID
     * @param page          페이지 번호 (1부터 시작)
     * @param pageSize      페이지 크기
     * @param sortDirection 정렬 방향
     * @param sortProperty  정렬 기준
     * @return 회원 리뷰 내역 조회 응답 {@link GetUserReviewsResponse}
     * @Author 성효빈
     * @Date 2026-04-01
     * @Description 관리자가 특정 회원의 리뷰 내역을 조회합니다.
     */
    @GetMapping("/admin/users/{userId}/reviews")
    @PreAuthorize(ADMIN_POINTCUT)
    @GetUserReviewsApiDocs
    public ResponseEntity<BaseResponse<GetUserReviewsResponse>> getUserReviews(
            @PathVariable("userId") Long userId,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "page-size", required = false, defaultValue = DEFAULT_PAGE_SIZE) int pageSize,
            @RequestParam(value = "sort-direction", required = false, defaultValue = "DESC") Sort.Direction sortDirection,
            @RequestParam(value = "sort-property", required = false, defaultValue = "ID") ReviewSortProperty sortProperty
    ) {
        GetUserReviewsResult result = getUserReviewsUseCase.getUserReviews(
                new GetUserReviewsCommand(userId, page, pageSize, sortDirection, sortProperty)
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        GetUserReviewsResponse.from(result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "회원 리뷰 내역 조회 성공"
                ),
                HttpStatus.OK
        );
    }
}
