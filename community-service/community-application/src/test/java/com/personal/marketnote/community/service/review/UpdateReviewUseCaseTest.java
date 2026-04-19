package com.personal.marketnote.community.service.review;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.community.domain.review.Review;
import com.personal.marketnote.community.domain.review.ReviewSnapshotState;
import com.personal.marketnote.community.exception.InvalidReviewContentContainsProfanityException;
import com.personal.marketnote.community.port.in.command.review.UpdateReviewCommand;
import com.personal.marketnote.community.port.in.usecase.review.GetReviewUseCase;
import com.personal.marketnote.community.port.out.event.PublishReviewEventPort;
import com.personal.marketnote.community.port.out.profanity.FindProfanityWordPort;
import com.personal.marketnote.community.port.out.review.SaveReviewPort;
import com.personal.marketnote.community.port.out.review.UpdateReviewPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateReviewUseCaseTest {
    @Mock
    private GetReviewUseCase getReviewUseCase;
    @Mock
    private SaveReviewPort saveReviewPort;
    @Mock
    private UpdateReviewPort updateReviewPort;
    @Mock
    private PublishReviewEventPort publishReviewEventPort;
    @Mock
    private FindProfanityWordPort findProfanityWordPort;

    @InjectMocks
    private UpdateReviewService updateReviewService;

    @Test
    @DisplayName("리뷰 수정 시 내용에 욕설이 포함되면 InvalidReviewContentContainsProfanityException이 발생한다")
    void updateReview_contentContainsProfanity_throwsException() {
        Long reviewId = 1L;
        Long reviewerId = 10L;
        UpdateReviewCommand command = UpdateReviewCommand.builder()
                .id(reviewId)
                .reviewerId(reviewerId)
                .rating(4.0f)
                .content("욕설포함내용")
                .isPhoto(false)
                .build();
        when(findProfanityWordPort.containsProfanity("욕설포함내용")).thenReturn(true);

        assertThatThrownBy(() -> updateReviewService.updateReview(command))
                .isInstanceOf(InvalidReviewContentContainsProfanityException.class);

        verifyNoInteractions(getReviewUseCase);
        verifyNoInteractions(updateReviewPort);
    }

    @Test
    @DisplayName("리뷰 수정 시 내용에 욕설이 없으면 정상 수정된다")
    void updateReview_contentWithoutProfanity_succeeds() {
        Long reviewId = 1L;
        Long reviewerId = 10L;
        Long productId = 50L;
        Review review = buildReview(reviewId, reviewerId, productId, 5.0f);
        when(getReviewUseCase.getReview(reviewId)).thenReturn(review);
        when(getReviewUseCase.getProductReviewAggregate(productId))
                .thenReturn(buildProductReviewAggregate(productId));
        UpdateReviewCommand command = UpdateReviewCommand.builder()
                .id(reviewId)
                .reviewerId(reviewerId)
                .rating(4.0f)
                .content("좋은 상품입니다")
                .isPhoto(false)
                .build();
        when(findProfanityWordPort.containsProfanity("좋은 상품입니다")).thenReturn(false);

        updateReviewService.updateReview(command);

        verify(findProfanityWordPort).containsProfanity("좋은 상품입니다");
        verify(updateReviewPort).update(review);
    }

    private Review buildReview(Long id, Long reviewerId) {
        return buildReview(id, reviewerId, 50L, 5.0f);
    }

    private Review buildReview(Long id, Long reviewerId, Long productId, Float rating) {
        return Review.from(
                ReviewSnapshotState.builder()
                        .id(id)
                        .reviewerId(reviewerId)
                        .orderId(100L)
                        .productId(productId)
                        .pricePolicyId(30L)
                        .productImageUrl("https://example.com/image.jpg")
                        .selectedOptions("30개입, 5박스")
                        .quantity(2)
                        .reviewerName("테스트유저")
                        .maskedReviewerName("테스트***")
                        .rating(rating)
                        .content("기존 리뷰 내용")
                        .isPhoto(false)
                        .isEdited(false)
                        .likeCount(0)
                        .status(EntityStatus.ACTIVE)
                        .createdAt(LocalDateTime.now())
                        .modifiedAt(LocalDateTime.now())
                        .orderNum(id)
                        .build()
        );
    }

    private com.personal.marketnote.community.domain.review.ProductReviewAggregate buildProductReviewAggregate(Long productId) {
        return com.personal.marketnote.community.domain.review.ProductReviewAggregate.from(
                buildReview(1L, 10L, productId, 5.0f)
        );
    }
}
