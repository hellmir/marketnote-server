package com.personal.marketnote.community.service.post;

import com.personal.marketnote.common.adapter.out.persistence.audit.EntityStatus;
import com.personal.marketnote.common.utility.ValueMasker;
import com.personal.marketnote.community.domain.post.*;
import com.personal.marketnote.community.exception.NotProductSellerException;
import com.personal.marketnote.community.port.in.command.post.RegisterPostCommand;
import com.personal.marketnote.community.port.in.result.post.RegisterPostResult;
import com.personal.marketnote.community.port.out.post.SavePostPort;
import com.personal.marketnote.community.port.out.product.FindProductByPricePolicyPort;
import com.personal.marketnote.community.port.out.result.product.ProductInfoResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterPostUseCaseTest {
    @Mock
    private SavePostPort savePostPort;
    @Mock
    private FindProductByPricePolicyPort findProductByPricePolicyPort;

    @InjectMocks
    private RegisterPostService registerPostService;

    @Test
    @DisplayName("일반 사용자가 게시글을 등록하면 저장된 게시글 ID를 반환한다")
    void registerPost_normalUser_returnsSavedPostId() {
        RegisterPostCommand command = buildCommand(1L, null, Board.ONE_ON_ONE_INQUERY, "ORDER_PAYMENT");
        Post savedPost = buildSavedPost(100L, command);
        when(savePostPort.save(any(Post.class))).thenReturn(savedPost);

        RegisterPostResult result = registerPostService.registerPost(false, command);

        assertThat(result.id()).isEqualTo(100L);
        verify(savePostPort).save(any(Post.class));
    }

    @Test
    @DisplayName("판매자가 답글이 아닌 게시글을 등록하면 상품 소유권 검증 없이 저장된다")
    void registerPost_sellerNotReply_skipsOwnershipValidation() {
        RegisterPostCommand command = buildCommand(2L, null, Board.PRODUCT_INQUERY, "PRODUCT_QUESTION");
        Post savedPost = buildSavedPost(200L, command);
        when(savePostPort.save(any(Post.class))).thenReturn(savedPost);

        RegisterPostResult result = registerPostService.registerPost(true, command);

        assertThat(result.id()).isEqualTo(200L);
        verifyNoInteractions(findProductByPricePolicyPort);
    }

    @Test
    @DisplayName("판매자가 자신의 상품에 대한 답글을 등록하면 성공한다")
    void registerPost_sellerReplyOwnProduct_succeeds() {
        Long sellerId = 3L;
        Long pricePolicyId = 500L;
        RegisterPostCommand command = buildReplyCommand(sellerId, 10L, pricePolicyId,
                Board.PRODUCT_INQUERY, "PRODUCT_QUESTION");
        Post savedPost = buildSavedPost(300L, command);

        ProductInfoResult productInfo = buildProductInfo(sellerId);
        when(findProductByPricePolicyPort.findByPricePolicyIds(List.of(pricePolicyId)))
                .thenReturn(Map.of(pricePolicyId, productInfo));
        when(savePostPort.save(any(Post.class))).thenReturn(savedPost);

        RegisterPostResult result = registerPostService.registerPost(true, command);

        assertThat(result.id()).isEqualTo(300L);
        verify(findProductByPricePolicyPort).findByPricePolicyIds(List.of(pricePolicyId));
        verify(savePostPort).save(any(Post.class));
    }

    @Test
    @DisplayName("판매자가 타인의 상품에 대한 답글을 등록하면 NotProductSellerException이 발생한다")
    void registerPost_sellerReplyOtherProduct_throwsNotProductSellerException() {
        Long sellerId = 4L;
        Long otherSellerId = 999L;
        Long pricePolicyId = 600L;
        RegisterPostCommand command = buildReplyCommand(sellerId, 20L, pricePolicyId,
                Board.PRODUCT_INQUERY, "PRODUCT_QUESTION");

        ProductInfoResult otherProduct = buildProductInfo(otherSellerId);
        when(findProductByPricePolicyPort.findByPricePolicyIds(List.of(pricePolicyId)))
                .thenReturn(Map.of(pricePolicyId, otherProduct));

        assertThatThrownBy(() -> registerPostService.registerPost(true, command))
                .isInstanceOf(NotProductSellerException.class)
                .hasMessageContaining(String.valueOf(pricePolicyId));

        verifyNoInteractions(savePostPort);
    }

    @Test
    @DisplayName("판매자 답글 등록 시 상품 정보가 조회되지 않으면 NotProductSellerException이 발생한다")
    void registerPost_sellerReplyProductNotFound_throwsNotProductSellerException() {
        Long pricePolicyId = 700L;
        RegisterPostCommand command = buildReplyCommand(5L, 30L, pricePolicyId,
                Board.PRODUCT_INQUERY, "PRODUCT_QUESTION");

        when(findProductByPricePolicyPort.findByPricePolicyIds(List.of(pricePolicyId)))
                .thenReturn(Map.of());

        assertThatThrownBy(() -> registerPostService.registerPost(true, command))
                .isInstanceOf(NotProductSellerException.class)
                .hasMessageContaining(String.valueOf(pricePolicyId));

        verifyNoInteractions(savePostPort);
    }

    @Test
    @DisplayName("일반 사용자 게시글 등록 시 FindProductByPricePolicyPort가 호출되지 않는다")
    void registerPost_normalUser_doesNotCallProductPort() {
        RegisterPostCommand command = buildCommand(6L, null, Board.ONE_ON_ONE_INQUERY, "DELIVERY");
        Post savedPost = buildSavedPost(400L, command);
        when(savePostPort.save(any(Post.class))).thenReturn(savedPost);

        registerPostService.registerPost(false, command);

        verifyNoInteractions(findProductByPricePolicyPort);
    }

    @Test
    @DisplayName("일반 사용자가 답글을 등록해도 상품 소유권 검증이 실행되지 않는다")
    void registerPost_normalUserReply_skipsOwnershipValidation() {
        RegisterPostCommand command = buildReplyCommand(7L, 40L, 800L,
                Board.PRODUCT_INQUERY, "PRODUCT_QUESTION");
        Post savedPost = buildSavedPost(500L, command);
        when(savePostPort.save(any(Post.class))).thenReturn(savedPost);

        RegisterPostResult result = registerPostService.registerPost(false, command);

        assertThat(result.id()).isEqualTo(500L);
        verifyNoInteractions(findProductByPricePolicyPort);
    }

    @Test
    @DisplayName("판매자 답글 등록 시 targetId가 FindProductByPricePolicyPort에 전달된다")
    void registerPost_sellerReply_passesTargetIdToProductPort() {
        Long pricePolicyId = 900L;
        Long sellerId = 8L;
        RegisterPostCommand command = buildReplyCommand(sellerId, 50L, pricePolicyId,
                Board.PRODUCT_INQUERY, "SHIPPING");
        Post savedPost = buildSavedPost(600L, command);

        ProductInfoResult productInfo = buildProductInfo(sellerId);
        when(findProductByPricePolicyPort.findByPricePolicyIds(List.of(pricePolicyId)))
                .thenReturn(Map.of(pricePolicyId, productInfo));
        when(savePostPort.save(any(Post.class))).thenReturn(savedPost);

        registerPostService.registerPost(true, command);

        ArgumentCaptor<List<Long>> idsCaptor = ArgumentCaptor.forClass(List.class);
        verify(findProductByPricePolicyPort).findByPricePolicyIds(idsCaptor.capture());
        assertThat(idsCaptor.getValue()).containsExactly(pricePolicyId);
    }

    @Test
    @DisplayName("게시글 등록 시 커맨드의 필드들이 Post 도메인 객체로 올바르게 매핑된다")
    void registerPost_commandFieldsMappedToPost() {
        Long userId = 9L;
        RegisterPostCommand command = RegisterPostCommand.builder()
                .userId(userId)
                .parentId(null)
                .board(Board.PRODUCT_INQUERY)
                .category("RESTOCK")
                .targetGroupType(PostTargetGroupType.PRODUCT)
                .targetGroupId(77L)
                .targetType(PostTargetType.PRICE_POLICY)
                .targetId(88L)
                .productImageUrl("https://example.com/img.jpg")
                .writerName("테스트유저")
                .title("문의합니다")
                .content("재입고 예정이 있나요?")
                .isPrivate(true)
                .isPhoto(false)
                .build();

        Post savedPost = buildSavedPost(700L, command);
        when(savePostPort.save(any(Post.class))).thenReturn(savedPost);

        registerPostService.registerPost(false, command);

        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
        verify(savePostPort).save(postCaptor.capture());
        Post captured = postCaptor.getValue();

        assertThat(captured.getUserId()).isEqualTo(userId);
        assertThat(captured.getBoard()).isEqualTo(Board.PRODUCT_INQUERY);
        assertThat(captured.getTargetGroupType()).isEqualTo(PostTargetGroupType.PRODUCT);
        assertThat(captured.getTargetGroupId()).isEqualTo(77L);
        assertThat(captured.getTargetType()).isEqualTo(PostTargetType.PRICE_POLICY);
        assertThat(captured.getTargetId()).isEqualTo(88L);
        assertThat(captured.getProductImageUrl()).isEqualTo("https://example.com/img.jpg");
        assertThat(captured.getWriterName()).isEqualTo("테스트유저");
        assertThat(captured.getMaskedWriterName()).isEqualTo("테스트***");
        assertThat(captured.getTitle()).isEqualTo("문의합니다");
        assertThat(captured.getContent()).isEqualTo("재입고 예정이 있나요?");
        assertThat(captured.isPrivate()).isTrue();
        assertThat(captured.isPhoto()).isFalse();
        assertThat(captured.getStatus()).isEqualTo(EntityStatus.ACTIVE);
    }

    @Test
    @DisplayName("SavePostPort.save 실행 중 예외가 발생하면 전파된다")
    void registerPost_savePortFails_propagatesException() {
        RegisterPostCommand command = buildCommand(10L, null, Board.FAQ, "ORDER_PAYMENT");
        RuntimeException exception = new RuntimeException("save failed");
        when(savePostPort.save(any(Post.class))).thenThrow(exception);

        assertThatThrownBy(() -> registerPostService.registerPost(false, command))
                .isSameAs(exception);
    }

    @Test
    @DisplayName("FindProductByPricePolicyPort 호출 중 예외가 발생하면 전파된다")
    void registerPost_productPortFails_propagatesException() {
        Long pricePolicyId = 1000L;
        RegisterPostCommand command = buildReplyCommand(11L, 60L, pricePolicyId,
                Board.PRODUCT_INQUERY, "PRODUCT_QUESTION");
        RuntimeException exception = new RuntimeException("product port failed");

        when(findProductByPricePolicyPort.findByPricePolicyIds(List.of(pricePolicyId)))
                .thenThrow(exception);

        assertThatThrownBy(() -> registerPostService.registerPost(true, command))
                .isSameAs(exception);

        verifyNoInteractions(savePostPort);
    }

    @Test
    @DisplayName("판매자 답글의 상품 소유권 검증 실패 시 예외 메시지에 가격 정책 ID가 포함된다")
    void registerPost_notProductSeller_exceptionContainsPricePolicyId() {
        Long pricePolicyId = 1100L;
        RegisterPostCommand command = buildReplyCommand(12L, 70L, pricePolicyId,
                Board.PRODUCT_INQUERY, "PRODUCT_QUESTION");

        ProductInfoResult otherProduct = buildProductInfo(999L);
        when(findProductByPricePolicyPort.findByPricePolicyIds(List.of(pricePolicyId)))
                .thenReturn(Map.of(pricePolicyId, otherProduct));

        assertThatThrownBy(() -> registerPostService.registerPost(true, command))
                .isInstanceOf(NotProductSellerException.class)
                .hasMessageContaining("1100");
    }

    @Test
    @DisplayName("1:1 문의 등록 시 maskedWriterName에 마스킹 없이 원본 작성자명이 저장된다")
    void registerPost_oneOnOneInquery_maskedWriterNameIsOriginal() {
        RegisterPostCommand command = RegisterPostCommand.builder()
                .userId(14L)
                .parentId(null)
                .board(Board.ONE_ON_ONE_INQUERY)
                .category("ORDER_PAYMENT")
                .writerName("테스트유저")
                .content("1:1 문의 내용입니다")
                .build();
        Post savedPost = buildSavedPost(900L, command);
        when(savePostPort.save(any(Post.class))).thenReturn(savedPost);

        registerPostService.registerPost(false, command);

        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
        verify(savePostPort).save(postCaptor.capture());
        Post captured = postCaptor.getValue();

        assertThat(captured.getMaskedWriterName()).isEqualTo("테스트유저");
    }

    @Test
    @DisplayName("상품 문의 등록 시 maskedWriterName에 마스킹된 작성자명이 저장된다")
    void registerPost_productInquery_maskedWriterNameIsMasked() {
        RegisterPostCommand command = RegisterPostCommand.builder()
                .userId(15L)
                .parentId(null)
                .board(Board.PRODUCT_INQUERY)
                .category("PRODUCT_QUESTION")
                .writerName("테스트유저")
                .content("상품 문의 내용입니다")
                .build();
        Post savedPost = buildSavedPost(1000L, command);
        when(savePostPort.save(any(Post.class))).thenReturn(savedPost);

        registerPostService.registerPost(false, command);

        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
        verify(savePostPort).save(postCaptor.capture());
        Post captured = postCaptor.getValue();

        assertThat(captured.getMaskedWriterName()).isEqualTo(ValueMasker.mask("테스트유저"));
    }

    @Test
    @DisplayName("parentId가 null이면 판매자여도 상품 소유권 검증을 건너뛴다")
    void registerPost_sellerNullParentId_skipsValidation() {
        RegisterPostCommand command = RegisterPostCommand.builder()
                .userId(13L)
                .parentId(null)
                .board(Board.PRODUCT_INQUERY)
                .category("PRODUCT_QUESTION")
                .writerName("판매자")
                .content("답변입니다")
                .build();
        Post savedPost = buildSavedPost(800L, command);
        when(savePostPort.save(any(Post.class))).thenReturn(savedPost);

        assertThat(command.isReply()).isFalse();

        RegisterPostResult result = registerPostService.registerPost(true, command);

        assertThat(result.id()).isEqualTo(800L);
        verifyNoInteractions(findProductByPricePolicyPort);
    }

    private RegisterPostCommand buildCommand(
            Long userId, Long parentId, Board board, String category
    ) {
        return RegisterPostCommand.builder()
                .userId(userId)
                .parentId(parentId)
                .board(board)
                .category(category)
                .writerName("작성자")
                .content("게시글 내용입니다")
                .build();
    }

    private RegisterPostCommand buildReplyCommand(
            Long userId, Long parentId, Long targetId, Board board, String category
    ) {
        return RegisterPostCommand.builder()
                .userId(userId)
                .parentId(parentId)
                .board(board)
                .category(category)
                .targetGroupType(PostTargetGroupType.PRODUCT)
                .targetGroupId(1L)
                .targetType(PostTargetType.PRICE_POLICY)
                .targetId(targetId)
                .writerName("작성자")
                .content("답글 내용입니다")
                .build();
    }

    private Post buildSavedPost(Long id, RegisterPostCommand command) {
        String maskedName = command.board().requiresWriterMasking()
                ? ValueMasker.mask(command.writerName())
                : command.writerName();
        return Post.from(
                PostSnapshotState.builder()
                        .id(id)
                        .userId(command.userId())
                        .parentId(command.parentId())
                        .board(command.board())
                        .category(command.category())
                        .targetGroupType(command.targetGroupType())
                        .targetGroupId(command.targetGroupId())
                        .targetType(command.targetType())
                        .targetId(command.targetId())
                        .productImageUrl(command.productImageUrl())
                        .writerName(command.writerName())
                        .maskedWriterName(maskedName)
                        .title(command.title())
                        .content(command.content())
                        .isPrivate(command.isPrivate())
                        .isPhoto(command.isPhoto())
                        .status(EntityStatus.ACTIVE)
                        .createdAt(LocalDateTime.now())
                        .modifiedAt(LocalDateTime.now())
                        .orderNum(id)
                        .build()
        );
    }

    private ProductInfoResult buildProductInfo(Long sellerId) {
        return new ProductInfoResult(sellerId, "상품명", "브랜드명", null, List.of(), null);
    }
}
