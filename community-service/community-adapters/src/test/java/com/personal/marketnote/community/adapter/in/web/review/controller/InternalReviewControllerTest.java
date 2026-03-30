package com.personal.marketnote.community.adapter.in.web.review.controller;

import com.personal.marketnote.community.port.in.result.review.GetProductReviewAggregatesResult;
import com.personal.marketnote.community.port.in.result.review.ProductReviewAggregateSummaryResult;
import com.personal.marketnote.community.port.in.usecase.review.GetReviewUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("InternalReviewController 테스트")
class InternalReviewControllerTest {
    @InjectMocks
    private InternalReviewController internalReviewController;

    @Mock
    private GetReviewUseCase getReviewUseCase;

    @Nested
    @DisplayName("getProductReviewAggregates")
    class GetProductReviewAggregates {
        @Test
        @DisplayName("상품 ID 목록으로 리뷰 집계를 조회하면 200 OK를 반환한다")
        void shouldReturnOkWhenGetProductReviewAggregatesSucceeds() {
            // given
            List<Long> productIds = List.of(1L, 2L);
            GetProductReviewAggregatesResult result = GetProductReviewAggregatesResult.from(
                    List.of(
                            new ProductReviewAggregateSummaryResult(1L, 10, 4.5f),
                            new ProductReviewAggregateSummaryResult(2L, 5, 3.0f)
                    )
            );
            when(getReviewUseCase.getProductReviewAggregates(productIds)).thenReturn(result);

            // when
            ResponseEntity<?> response = internalReviewController.getProductReviewAggregates(productIds);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(getReviewUseCase).getProductReviewAggregates(productIds);
        }
    }
}
