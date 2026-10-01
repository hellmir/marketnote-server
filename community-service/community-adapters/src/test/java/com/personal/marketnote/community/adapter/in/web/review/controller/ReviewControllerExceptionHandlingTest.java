package com.personal.marketnote.community.adapter.in.web.review.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.common.domain.exception.token.AuthenticationFailedException;
import com.personal.marketnote.community.adapter.in.web.review.response.GetProductReviewAggregateResponse;
import com.personal.marketnote.community.domain.review.ProductReviewAggregate;
import com.personal.marketnote.community.domain.review.ProductReviewAggregateSnapshotState;
import com.personal.marketnote.community.exception.ProductReviewAggregateNotFoundException;
import com.personal.marketnote.community.port.in.usecase.review.DeleteReviewUseCase;
import com.personal.marketnote.community.port.in.usecase.review.GetReviewKeyUseCase;
import com.personal.marketnote.community.port.in.usecase.review.GetReviewUseCase;
import com.personal.marketnote.community.port.in.usecase.review.GetUserReviewsUseCase;
import com.personal.marketnote.community.port.in.usecase.review.RegisterReviewUseCase;
import com.personal.marketnote.community.port.in.usecase.review.UpdateReviewUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewController 예외 핸들링")
class ReviewControllerExceptionHandlingTest {

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

    @Nested
    @DisplayName("getProductReviewAggregate 예외 처리")
    class GetProductReviewAggregate {

        @Test
        @DisplayName("집계가 존재하면 OK + 점수별 카운트를 매핑한다")
        void returnsAggregateWhenFound() {
            ProductReviewAggregate aggregate = ProductReviewAggregate.from(
                    ProductReviewAggregateSnapshotState.builder()
                            .productId(1L)
                            .totalCount(3)
                            .fivePointCount(2)
                            .fourPointCount(1)
                            .threePointCount(0)
                            .twoPointCount(0)
                            .onePointCount(0)
                            .totalRating(14f)
                            .averageRating(4.67f)
                            .build()
            );
            when(getReviewUseCase.getProductReviewAggregate(1L)).thenReturn(aggregate);

            ResponseEntity<BaseResponse<GetProductReviewAggregateResponse>> response =
                    controller.getProductReviewAggregate(1L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent().totalCount()).isEqualTo(3);
            assertThat(response.getBody().getContent().fivePointCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("ProductReviewAggregateNotFoundException 발생 시 0건 응답으로 fallback 한다")
        void returnsEmptyAggregateOnNotFound() {
            when(getReviewUseCase.getProductReviewAggregate(1L))
                    .thenThrow(new ProductReviewAggregateNotFoundException(1L));

            ResponseEntity<BaseResponse<GetProductReviewAggregateResponse>> response =
                    controller.getProductReviewAggregate(1L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent().totalCount()).isZero();
            assertThat(response.getBody().getContent().averageRating()).isZero();
        }
    }

    @Nested
    @DisplayName("getProductReviews 인증 분기")
    class GetProductReviews {

        @Test
        @DisplayName("principal이 null이면 userId 없이 UseCase를 호출한다")
        void allowsAnonymousAccess() {
            when(getReviewUseCase.getProductReviews(eq(null), eq(1L), eq(false), any(), anyInt(), any(), any()))
                    .thenReturn(null);

            assertThatThrownBy(() -> controller.getProductReviews(
                    1L, false, null, 4,
                    org.springframework.data.domain.Sort.Direction.DESC,
                    com.personal.marketnote.community.domain.review.ReviewSortProperty.ID,
                    null
            )).isInstanceOf(NullPointerException.class);
            verify(getReviewUseCase).getProductReviews(eq(null), eq(1L), eq(false), any(), anyInt(), any(), any());
        }

        @Test
        @DisplayName("principal이 -1이면 AuthenticationFailedException")
        void throwsAuthFailedWhenPrincipalNameIsMinusOne() {
            OAuth2AuthenticatedPrincipal principal = principal("-1", "ROLE_BUYER");

            assertThatThrownBy(() -> controller.getProductReviews(
                    1L, false, null, 4,
                    org.springframework.data.domain.Sort.Direction.DESC,
                    com.personal.marketnote.community.domain.review.ReviewSortProperty.ID,
                    principal
            )).isInstanceOf(AuthenticationFailedException.class);
            verifyNoInteractions(getReviewUseCase);
        }
    }

    @Nested
    @DisplayName("registerReview / updateReview / deleteReview 인증")
    class WriterApisRequirePrincipal {

        @Test
        @DisplayName("registerReview principal이 null이면 AuthenticationFailedException")
        void registerReviewThrowsWhenPrincipalNull() {
            assertThatThrownBy(() -> controller.registerReview(null, null))
                    .isInstanceOf(AuthenticationFailedException.class);
            verifyNoInteractions(registerReviewUseCase);
        }

        @Test
        @DisplayName("updateReview principal이 null이면 AuthenticationFailedException")
        void updateReviewThrowsWhenPrincipalNull() {
            assertThatThrownBy(() -> controller.updateReview(1L, null, null))
                    .isInstanceOf(AuthenticationFailedException.class);
            verifyNoInteractions(updateReviewUseCase);
        }

        @Test
        @DisplayName("deleteReview principal이 null이면 AuthenticationFailedException")
        void deleteReviewThrowsWhenPrincipalNull() {
            assertThatThrownBy(() -> controller.deleteReview(1L, null))
                    .isInstanceOf(AuthenticationFailedException.class);
            verifyNoInteractions(deleteReviewUseCase);
        }
    }

    @Nested
    @DisplayName("getMyReviews / getMyReviewsCount / getReviewKey 인증")
    class MyReviewApisRequirePrincipal {

        @Test
        @DisplayName("getMyReviews principal이 null이면 AuthenticationFailedException")
        void getMyReviewsThrowsWhenPrincipalNull() {
            assertThatThrownBy(() -> controller.getMyReviews(
                    null, 4,
                    org.springframework.data.domain.Sort.Direction.DESC,
                    com.personal.marketnote.community.domain.review.ReviewSortProperty.ID,
                    null
            )).isInstanceOf(AuthenticationFailedException.class);
            verifyNoInteractions(getReviewUseCase);
        }

        @Test
        @DisplayName("getMyReviewsCount principal이 null이면 AuthenticationFailedException")
        void getMyReviewsCountThrowsWhenPrincipalNull() {
            assertThatThrownBy(() -> controller.getMyReviewsCount(null))
                    .isInstanceOf(AuthenticationFailedException.class);
            verifyNoInteractions(getReviewUseCase);
        }

        @Test
        @DisplayName("getReviewKey principal이 null이면 AuthenticationFailedException")
        void getReviewKeyThrowsWhenPrincipalNull() {
            assertThatThrownBy(() -> controller.getReviewKey(1L, null))
                    .isInstanceOf(AuthenticationFailedException.class);
            verifyNoInteractions(getReviewKeyUseCase);
        }
    }

    @Nested
    @DisplayName("getReview 인증 분기")
    class GetReview {

        @Test
        @DisplayName("principal이 null이면 userId 없이 UseCase를 호출한다")
        void allowsAnonymousReviewLookup() {
            when(getReviewUseCase.getReviewDetail(1L, null)).thenReturn(null);

            assertThatThrownBy(() -> controller.getReview(1L, null))
                    .isInstanceOf(NullPointerException.class);
            verify(getReviewUseCase).getReviewDetail(1L, null);
        }
    }

    private OAuth2AuthenticatedPrincipal principal(String name, String authority) {
        return new OAuth2AuthenticatedPrincipal() {
            @Override
            public Map<String, Object> getAttributes() {
                return Map.of();
            }

            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                return List.of(new SimpleGrantedAuthority(authority));
            }

            @Override
            public String getName() {
                return name;
            }
        };
    }

    private static <T> T eq(T value) {
        return org.mockito.ArgumentMatchers.eq(value);
    }

    private static <T> T any() {
        return org.mockito.ArgumentMatchers.any();
    }

    private static int anyInt() {
        return org.mockito.ArgumentMatchers.anyInt();
    }
}
