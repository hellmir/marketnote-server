package com.personal.marketnote.community.service.review;

import com.personal.marketnote.common.adapter.out.persistence.audit.EntityStatus;
import com.personal.marketnote.common.utility.ValueMasker;
import com.personal.marketnote.community.domain.review.Review;
import com.personal.marketnote.community.domain.review.ReviewSnapshotState;
import com.personal.marketnote.community.exception.InvalidReviewContentContainsProfanityException;
import com.personal.marketnote.community.exception.ProductReviewAggregateNotFoundException;
import com.personal.marketnote.community.exception.ReviewAlreadyExistsException;
import com.personal.marketnote.community.exception.UnauthorizedOrderAccessException;
import com.personal.marketnote.community.port.in.command.review.RegisterReviewCommand;
import com.personal.marketnote.community.port.in.usecase.review.GetReviewUseCase;
import com.personal.marketnote.community.port.out.event.PublishReviewEventPort;
import com.personal.marketnote.community.port.out.order.VerifyOrderOwnershipPort;
import com.personal.marketnote.community.port.out.profanity.FindProfanityWordPort;
import com.personal.marketnote.community.port.out.review.SaveReviewPort;
import com.personal.marketnote.community.port.out.review.UpdateReviewPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterReviewUseCaseTest {
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
    @Mock
    private VerifyOrderOwnershipPort verifyOrderOwnershipPort;

    @InjectMocks
    private RegisterReviewService registerReviewService;

    @Test
    @DisplayName("리뷰 등록 시 커맨드의 reviewerName이 Review 도메인의 reviewerName(원본)으로 매핑된다")
    void registerReview_reviewerNameMappedAsOriginal() {
        RegisterReviewCommand command = buildCommand("테스트유저");
        Review savedReview = buildSavedReview(1L, command);
        when(saveReviewPort.save(any(Review.class))).thenReturn(savedReview);
        when(getReviewUseCase.getProductReviewAggregate(anyLong()))
                .thenThrow(new ProductReviewAggregateNotFoundException(command.productId()));

        registerReviewService.registerReview(command);

        ArgumentCaptor<Review> reviewCaptor = ArgumentCaptor.forClass(Review.class);
        verify(saveReviewPort).save(reviewCaptor.capture());
        Review captured = reviewCaptor.getValue();

        assertThat(captured.getReviewerName()).isEqualTo("테스트유저");
    }

    @Test
    @DisplayName("리뷰 등록 시 커맨드의 reviewerName이 Review 도메인의 maskedReviewerName(마스킹)으로 매핑된다")
    void registerReview_reviewerNameMappedAsMasked() {
        RegisterReviewCommand command = buildCommand("테스트유저");
        Review savedReview = buildSavedReview(1L, command);
        when(saveReviewPort.save(any(Review.class))).thenReturn(savedReview);
        when(getReviewUseCase.getProductReviewAggregate(anyLong()))
                .thenThrow(new ProductReviewAggregateNotFoundException(command.productId()));

        registerReviewService.registerReview(command);

        ArgumentCaptor<Review> reviewCaptor = ArgumentCaptor.forClass(Review.class);
        verify(saveReviewPort).save(reviewCaptor.capture());
        Review captured = reviewCaptor.getValue();

        assertThat(captured.getMaskedReviewerName()).isEqualTo("테스트***");
    }

    private RegisterReviewCommand buildCommand(String reviewerName) {
        return buildCommand(reviewerName, "배송이 빠르고 포장 상태도 좋았습니다.");
    }

    private RegisterReviewCommand buildCommand(String reviewerName, String content) {
        return RegisterReviewCommand.builder()
                .reviewerId(1L)
                .orderId(100L)
                .productId(50L)
                .pricePolicyId(30L)
                .productImageUrl("https://example.com/image.jpg")
                .selectedOptions("30개입, 5박스")
                .quantity(2)
                .reviewerName(reviewerName)
                .rating(5.0f)
                .content(content)
                .isPhoto(false)
                .build();
    }

    @Test
    @DisplayName("리뷰 등록 시 내용에 욕설이 포함되면 InvalidReviewContentContainsProfanityException이 발생한다")
    void registerReview_contentContainsProfanity_throwsException() {
        RegisterReviewCommand command = buildCommand("테스트유저", "욕설포함내용");
        when(findProfanityWordPort.containsProfanity("욕설포함내용")).thenReturn(true);

        assertThatThrownBy(() -> registerReviewService.registerReview(command))
                .isInstanceOf(InvalidReviewContentContainsProfanityException.class);

        verifyNoInteractions(saveReviewPort);
    }

    @Test
    @DisplayName("리뷰 등록 시 내용에 욕설이 없으면 정상 등록된다")
    void registerReview_contentWithoutProfanity_succeeds() {
        RegisterReviewCommand command = buildCommand("테스트유저", "좋은 상품입니다");
        when(findProfanityWordPort.containsProfanity("좋은 상품입니다")).thenReturn(false);
        Review savedReview = buildSavedReview(1L, command);
        when(saveReviewPort.save(any(Review.class))).thenReturn(savedReview);
        when(getReviewUseCase.getProductReviewAggregate(anyLong()))
                .thenThrow(new ProductReviewAggregateNotFoundException(command.productId()));

        registerReviewService.registerReview(command);

        verify(findProfanityWordPort).containsProfanity("좋은 상품입니다");
        verify(saveReviewPort).save(any(Review.class));
    }

    @Test
    @DisplayName("리뷰 등록 시 주문 소유권 검증이 성공하면 리뷰가 정상 등록된다")
    void registerReview_orderOwnershipVerified_succeeds() {
        RegisterReviewCommand command = buildCommand("테스트유저");
        Review savedReview = buildSavedReview(1L, command);
        when(saveReviewPort.save(any(Review.class))).thenReturn(savedReview);
        when(getReviewUseCase.getProductReviewAggregate(command.productId()))
                .thenThrow(new ProductReviewAggregateNotFoundException(command.productId()));

        registerReviewService.registerReview(command);

        verify(verifyOrderOwnershipPort).verifyOrderOwnership(command.orderId(), command.reviewerId());
        verify(saveReviewPort).save(any(Review.class));
    }

    @Test
    @DisplayName("리뷰 등록 시 주문 소유권 검증이 실패하면 UnauthorizedOrderAccessException이 발생한다")
    void registerReview_orderOwnershipNotVerified_throwsException() {
        RegisterReviewCommand command = buildCommand("테스트유저");
        doThrow(new UnauthorizedOrderAccessException())
                .when(verifyOrderOwnershipPort).verifyOrderOwnership(command.orderId(), command.reviewerId());

        assertThatThrownBy(() -> registerReviewService.registerReview(command))
                .isInstanceOf(UnauthorizedOrderAccessException.class);

        verifyNoInteractions(saveReviewPort);
    }

    @Test
    @DisplayName("중복 리뷰 검증 실패 시 주문 소유권 검증을 수행하지 않는다")
    void registerReview_duplicateReview_skipsOwnershipVerification() {
        RegisterReviewCommand command = buildCommand("테스트유저");
        doThrow(new ReviewAlreadyExistsException(command.orderId(), command.pricePolicyId()))
                .when(getReviewUseCase).validateDuplicateReview(command);

        assertThatThrownBy(() -> registerReviewService.registerReview(command))
                .isInstanceOf(ReviewAlreadyExistsException.class);

        verifyNoInteractions(verifyOrderOwnershipPort);
        verifyNoInteractions(saveReviewPort);
    }

    @Test
    @DisplayName("비속어 검증 실패 시 주문 소유권 검증을 수행하지 않는다")
    void registerReview_profanityDetected_skipsOwnershipVerification() {
        RegisterReviewCommand command = buildCommand("테스트유저", "욕설포함내용");
        when(findProfanityWordPort.containsProfanity("욕설포함내용")).thenReturn(true);

        assertThatThrownBy(() -> registerReviewService.registerReview(command))
                .isInstanceOf(InvalidReviewContentContainsProfanityException.class);

        verifyNoInteractions(verifyOrderOwnershipPort);
        verifyNoInteractions(saveReviewPort);
    }

    private Review buildSavedReview(Long id, RegisterReviewCommand command) {
        return Review.from(
                ReviewSnapshotState.builder()
                        .id(id)
                        .reviewerId(command.reviewerId())
                        .orderId(command.orderId())
                        .productId(command.productId())
                        .pricePolicyId(command.pricePolicyId())
                        .productImageUrl(command.productImageUrl())
                        .selectedOptions(command.selectedOptions())
                        .quantity(command.quantity())
                        .reviewerName(command.reviewerName())
                        .maskedReviewerName(ValueMasker.mask(command.reviewerName()))
                        .rating(command.rating())
                        .content(command.content())
                        .isPhoto(command.isPhoto())
                        .isEdited(false)
                        .likeCount(0)
                        .status(EntityStatus.ACTIVE)
                        .createdAt(LocalDateTime.now())
                        .modifiedAt(LocalDateTime.now())
                        .orderNum(id)
                        .build()
        );
    }
}
