package com.personal.marketnote.community.adapter.in.web.review.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.community.adapter.in.web.review.request.RegisterReviewRequest;
import com.personal.marketnote.community.adapter.in.web.review.request.UpdateReviewRequest;
import com.personal.marketnote.community.adapter.in.web.review.response.GetMyReviewsResponse;
import com.personal.marketnote.community.adapter.in.web.review.response.GetProductReviewAggregatesResponse;
import com.personal.marketnote.community.adapter.in.web.review.response.GetReviewKeyResponse;
import com.personal.marketnote.community.adapter.in.web.review.response.GetReviewsCountResponse;
import com.personal.marketnote.community.adapter.in.web.review.response.GetReviewsResponse;
import com.personal.marketnote.community.adapter.in.web.review.response.GetUserReviewsResponse;
import com.personal.marketnote.community.adapter.in.web.review.response.RegisterReviewResponse;
import com.personal.marketnote.community.adapter.in.web.review.response.ReviewItemResponse;
import com.personal.marketnote.community.adapter.in.web.review.response.UpdateReviewResponse;
import com.personal.marketnote.community.domain.review.ProductReviewAggregate;
import com.personal.marketnote.community.domain.review.ProductReviewAggregateSnapshotState;
import com.personal.marketnote.community.domain.review.Rating;
import com.personal.marketnote.community.domain.review.Review;
import com.personal.marketnote.community.domain.review.ReviewCreateState;
import com.personal.marketnote.community.domain.review.ReviewSortProperty;
import com.personal.marketnote.community.port.in.command.review.GetUserReviewsCommand;
import com.personal.marketnote.community.port.in.command.review.RegisterReviewCommand;
import com.personal.marketnote.community.port.in.command.review.UpdateReviewCommand;
import com.personal.marketnote.community.port.in.result.review.GetMyReviewsResult;
import com.personal.marketnote.community.port.in.result.review.GetProductReviewAggregatesResult;
import com.personal.marketnote.community.port.in.result.review.GetReviewCountResult;
import com.personal.marketnote.community.port.in.result.review.GetReviewKeyResult;
import com.personal.marketnote.community.port.in.result.review.GetReviewsResult;
import com.personal.marketnote.community.port.in.result.review.GetUserReviewsResult;
import com.personal.marketnote.community.port.in.result.review.RegisterReviewResult;
import com.personal.marketnote.community.port.in.result.review.ReviewItemResult;
import com.personal.marketnote.community.port.in.result.review.UpdateReviewResult;
import com.personal.marketnote.community.port.in.usecase.review.DeleteReviewUseCase;
import com.personal.marketnote.community.port.in.usecase.review.GetReviewKeyUseCase;
import com.personal.marketnote.community.port.in.usecase.review.GetReviewUseCase;
import com.personal.marketnote.community.port.in.usecase.review.GetUserReviewsUseCase;
import com.personal.marketnote.community.port.in.usecase.review.RegisterReviewUseCase;
import com.personal.marketnote.community.port.in.usecase.review.UpdateReviewUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewController")
class ReviewControllerTest {

    @Mock
    private RegisterReviewUseCase registerReviewUseCase;

    @Mock
    private GetReviewUseCase getReviewUseCase;

    @Mock
    private GetReviewKeyUseCase getReviewKeyUseCase;

    @Mock
    private GetUserReviewsUseCase getUserReviewsUseCase;

    @Mock
    private UpdateReviewUseCase updateReviewUseCase;

    @Mock
    private DeleteReviewUseCase deleteReviewUseCase;

    @InjectMocks
    private ReviewController controller;

    private OAuth2AuthenticatedPrincipal principal;
    private Review sampleReview;

    @BeforeEach
    void setUp() {
        principal = buildPrincipal(10L);
        sampleReview = Review.from(
                ReviewCreateState.builder()
                        .reviewerId(10L)
                        .orderId(100L)
                        .productId(1000L)
                        .pricePolicyId(10000L)
                        .productImageUrl("https://cdn.example.com/product.png")
                        .selectedOptions("RED")
                        .quantity(1)
                        .reviewerName("리뷰어")
                        .rating(Rating.of(5f))
                        .content("테스트용 리뷰 내용입니다.")
                        .isPhoto(false)
                        .unitAmount(10000L)
                        .build()
        );
        ReflectionTestUtils.setField(sampleReview, "id", 77L);
        ReflectionTestUtils.setField(sampleReview, "reviewKey", UUID.randomUUID());
    }

    @Nested
    @DisplayName("registerReview")
    class RegisterReview {

        @Test
        @DisplayName("정상 요청 시 201 CREATED와 리뷰 ID를 반환한다")
        void returnsCreatedWithReviewId() {
            RegisterReviewRequest request = buildRegisterRequest();
            RegisterReviewResult result = RegisterReviewResult.from(sampleReview);
            when(registerReviewUseCase.registerReview(any(RegisterReviewCommand.class))).thenReturn(result);

            ResponseEntity<BaseResponse<RegisterReviewResponse>> response =
                    controller.registerReview(request, principal);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getCode()).isEqualTo("SUC01");
            assertThat(response.getBody().getContent().id()).isEqualTo(77L);
            verify(registerReviewUseCase).registerReview(any(RegisterReviewCommand.class));
        }
    }

    @Nested
    @DisplayName("getProductReviews")
    class GetProductReviews {

        @Test
        @DisplayName("인증된 사용자가 호출하면 userId와 함께 UseCase에 위임한다")
        void delegatesWithUserIdWhenAuthenticated() {
            GetReviewsResult result = new GetReviewsResult(0L, -1L, false, List.of());
            when(getReviewUseCase.getProductReviews(
                    10L, 1000L, false, null, 4, Sort.Direction.DESC, ReviewSortProperty.ID
            )).thenReturn(result);

            ResponseEntity<BaseResponse<GetReviewsResponse>> response = controller.getProductReviews(
                    1000L, false, null, 4, Sort.Direction.DESC, ReviewSortProperty.ID, principal
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            verify(getReviewUseCase).getProductReviews(
                    10L, 1000L, false, null, 4, Sort.Direction.DESC, ReviewSortProperty.ID
            );
        }
    }

    @Nested
    @DisplayName("getMyReviews")
    class GetMyReviews {

        @Test
        @DisplayName("인증된 사용자의 리뷰 목록을 조회해 OK 응답을 반환한다")
        void returnsMyReviews() {
            GetMyReviewsResult result = new GetMyReviewsResult(0L, -1L, false, List.of());
            when(getReviewUseCase.getWriterReviews(10L, null, 4, Sort.Direction.DESC, ReviewSortProperty.ID))
                    .thenReturn(result);

            ResponseEntity<BaseResponse<GetMyReviewsResponse>> response = controller.getMyReviews(
                    null, 4, Sort.Direction.DESC, ReviewSortProperty.ID, principal
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            verify(getReviewUseCase).getWriterReviews(10L, null, 4, Sort.Direction.DESC, ReviewSortProperty.ID);
        }
    }

    @Nested
    @DisplayName("getReview")
    class GetReview {

        @Test
        @DisplayName("인증된 사용자가 조회하면 리뷰 상세를 반환한다")
        void returnsReviewDetailWhenAuthenticated() {
            ReviewItemResult result = ReviewItemResult.from(sampleReview);
            when(getReviewUseCase.getReviewDetail(77L, 10L)).thenReturn(result);

            ResponseEntity<BaseResponse<ReviewItemResponse>> response = controller.getReview(77L, principal);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent().id()).isEqualTo(77L);
        }
    }

    @Nested
    @DisplayName("getReviewKey")
    class GetReviewKey {

        @Test
        @DisplayName("reviewKey를 조회해 OK 응답을 반환한다")
        void returnsReviewKey() {
            GetReviewKeyResult result = GetReviewKeyResult.from(sampleReview);
            when(getReviewKeyUseCase.getReviewKey(77L, 10L)).thenReturn(result);

            ResponseEntity<BaseResponse<GetReviewKeyResponse>> response = controller.getReviewKey(77L, principal);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent().reviewKey()).isEqualTo(sampleReview.getReviewKey());
        }
    }

    @Nested
    @DisplayName("getMyReviewsCount")
    class GetMyReviewsCount {

        @Test
        @DisplayName("나의 리뷰 개수를 조회해 OK 응답을 반환한다")
        void returnsMyReviewCount() {
            when(getReviewUseCase.getWriterReviewCount(10L))
                    .thenReturn(GetReviewCountResult.of(5L));

            ResponseEntity<BaseResponse<GetReviewsCountResponse>> response =
                    controller.getMyReviewsCount(principal);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent().totalCount()).isEqualTo(5L);
        }
    }

    @Nested
    @DisplayName("updateReview")
    class UpdateReview {

        @Test
        @DisplayName("정상 요청 시 OK와 수정된 리뷰 ID를 반환한다")
        void returnsOkWithUpdatedReviewId() {
            UpdateReviewRequest request = buildUpdateRequest();
            UpdateReviewResult result = UpdateReviewResult.from(sampleReview);
            when(updateReviewUseCase.updateReview(any(UpdateReviewCommand.class))).thenReturn(result);

            ResponseEntity<BaseResponse<UpdateReviewResponse>> response =
                    controller.updateReview(77L, request, principal);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent().id()).isEqualTo(77L);
        }
    }

    @Nested
    @DisplayName("deleteReview")
    class DeleteReview {

        @Test
        @DisplayName("정상 요청 시 OK 응답을 반환하고 UseCase에 위임한다")
        void delegatesDeletionAndReturnsOk() {
            ResponseEntity<BaseResponse<Void>> response = controller.deleteReview(77L, principal);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(deleteReviewUseCase).deleteReview(77L, 10L);
        }
    }

    @Nested
    @DisplayName("getProductReviewAggregates")
    class GetProductReviewAggregates {

        @Test
        @DisplayName("상품 ID 목록으로 집계 결과를 조회해 OK 응답을 반환한다")
        void returnsAggregates() {
            GetProductReviewAggregatesResult result = GetProductReviewAggregatesResult.empty();
            when(getReviewUseCase.getProductReviewAggregates(List.of(1L, 2L))).thenReturn(result);

            ResponseEntity<BaseResponse<GetProductReviewAggregatesResponse>> response =
                    controller.getProductReviewAggregates(List.of(1L, 2L));

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
        }
    }

    @Nested
    @DisplayName("getUserReviews")
    class GetUserReviewsEndpoint {

        @Test
        @DisplayName("관리자가 회원 ID로 리뷰 내역을 조회하면 Command를 조립해 UseCase에 위임한다")
        void delegatesToUseCaseWithCommand() {
            GetUserReviewsResult result = GetUserReviewsResult.of(1, 10, 0L, 0, List.of());
            ArgumentCaptor<GetUserReviewsCommand> captor = ArgumentCaptor.forClass(GetUserReviewsCommand.class);
            when(getUserReviewsUseCase.getUserReviews(captor.capture())).thenReturn(result);

            ResponseEntity<BaseResponse<GetUserReviewsResponse>> response = controller.getUserReviews(
                    10L, 1, 10, Sort.Direction.DESC, ReviewSortProperty.ID
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            GetUserReviewsCommand command = captor.getValue();
            assertThat(command.userId()).isEqualTo(10L);
            assertThat(command.page()).isEqualTo(1);
            assertThat(command.pageSize()).isEqualTo(10);
            assertThat(command.sortDirection()).isEqualTo(Sort.Direction.DESC);
            assertThat(command.sortProperty()).isEqualTo(ReviewSortProperty.ID);
        }
    }

    @Nested
    @DisplayName("getProductReviewAggregate - 집계 조회")
    class GetProductReviewAggregate {

        @Test
        @DisplayName("집계가 존재하면 점수별 카운트를 매핑해 OK 응답을 반환한다")
        void returnsAggregateWhenFound() {
            ProductReviewAggregate aggregate = ProductReviewAggregate.from(
                    ProductReviewAggregateSnapshotState.builder()
                            .productId(1000L)
                            .totalCount(5)
                            .fivePointCount(3)
                            .fourPointCount(2)
                            .threePointCount(0)
                            .twoPointCount(0)
                            .onePointCount(0)
                            .totalRating(23f)
                            .averageRating(4.6f)
                            .build()
            );
            when(getReviewUseCase.getProductReviewAggregate(1000L)).thenReturn(aggregate);

            ResponseEntity<BaseResponse<GetProductReviewAggregatesResponse>> ignored;
            ResponseEntity<BaseResponse<com.personal.marketnote.community.adapter.in.web.review.response.GetProductReviewAggregateResponse>> response =
                    controller.getProductReviewAggregate(1000L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getContent().totalCount()).isEqualTo(5);
            assertThat(response.getBody().getContent().fivePointCount()).isEqualTo(3);
            verifyNoInteractions(registerReviewUseCase);
        }
    }

    private RegisterReviewRequest buildRegisterRequest() {
        RegisterReviewRequest request = new RegisterReviewRequest();
        ReflectionTestUtils.setField(request, "orderId", 100L);
        ReflectionTestUtils.setField(request, "productId", 1000L);
        ReflectionTestUtils.setField(request, "pricePolicyId", 10000L);
        ReflectionTestUtils.setField(request, "productImageUrl", "https://cdn.example.com/product.png");
        ReflectionTestUtils.setField(request, "selectedOptions", "RED");
        ReflectionTestUtils.setField(request, "quantity", 1);
        ReflectionTestUtils.setField(request, "reviewerName", "리뷰어");
        ReflectionTestUtils.setField(request, "rating", 5f);
        ReflectionTestUtils.setField(request, "content", "테스트용 리뷰 내용입니다.");
        ReflectionTestUtils.setField(request, "isPhoto", false);
        ReflectionTestUtils.setField(request, "unitAmount", 10000L);
        return request;
    }

    private UpdateReviewRequest buildUpdateRequest() {
        UpdateReviewRequest request = new UpdateReviewRequest();
        ReflectionTestUtils.setField(request, "rating", 4f);
        ReflectionTestUtils.setField(request, "content", "수정된 리뷰 내용입니다.");
        ReflectionTestUtils.setField(request, "isPhoto", false);
        return request;
    }

    private OAuth2AuthenticatedPrincipal buildPrincipal(Long userId) {
        return new DefaultOAuth2AuthenticatedPrincipal(
                String.valueOf(userId), Map.of("name", String.valueOf(userId)), List.of()
        );
    }

    private static <T> T any(Class<T> type) {
        return org.mockito.ArgumentMatchers.any(type);
    }
}
