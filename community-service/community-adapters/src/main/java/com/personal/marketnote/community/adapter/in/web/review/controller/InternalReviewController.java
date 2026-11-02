package com.personal.marketnote.community.adapter.in.web.review.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.community.adapter.in.web.review.response.GetProductReviewAggregatesResponse;
import com.personal.marketnote.community.port.in.usecase.review.GetReviewUseCase;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.personal.marketnote.common.domain.exception.ExceptionCode.DEFAULT_SUCCESS_CODE;

/**
 * 내부 리뷰 컨트롤러 (서비스 간 통신용)
 *
 * @Author 성효빈
 * @Date 2026-03-28
 * @Description HMAC 인증 기반 서비스 간 통신용 리뷰 API를 제공합니다.
 */
@RestController
@RequestMapping("/api/v1/internal")
@Tag(
        name = "내부 리뷰 API",
        description = "서비스 간 통신용 리뷰 API"
)
@RequiredArgsConstructor
public class InternalReviewController {
    private final GetReviewUseCase getReviewUseCase;

    /**
     * 상품 리뷰 집계 목록 조회 (서비스 간 통신용)
     *
     * @param productIds 상품 ID 목록
     * @return 상품 리뷰 집계 목록 조회 응답 {@link GetProductReviewAggregatesResponse}
     */
    @GetMapping("products/review-aggregates")
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
}
