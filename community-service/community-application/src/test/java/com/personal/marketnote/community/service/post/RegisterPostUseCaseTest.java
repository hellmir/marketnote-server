package com.personal.marketnote.community.service.post;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.common.utility.ValueMasker;
import com.personal.marketnote.community.domain.post.*;
import com.personal.marketnote.community.exception.InvalidPostContentContainsProfanityException;
import com.personal.marketnote.community.exception.NotProductSellerException;
import com.personal.marketnote.community.port.in.command.post.RegisterPostCommand;
import com.personal.marketnote.community.port.in.result.post.RegisterPostResult;
import com.personal.marketnote.community.port.out.event.PublishPostEventPort;
import com.personal.marketnote.community.port.out.post.FindPostPort;
import com.personal.marketnote.community.port.out.post.SavePostPort;
import com.personal.marketnote.community.port.out.product.FindProductByPricePolicyPort;
import com.personal.marketnote.community.port.out.profanity.FindProfanityWordPort;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterPostUseCaseTest {
    @Mock
    private SavePostPort savePostPort;
    @Mock
    private FindPostPort findPostPort;
    @Mock
    private FindProductByPricePolicyPort findProductByPricePolicyPort;
    @Mock
    private FindProfanityWordPort findProfanityWordPort;
    @Mock
    private PublishPostEventPort publishPostEventPort;

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
    @DisplayName("게시글을 등록하면 저장된 Post의 postKey가 Result에 포함된다")
    void registerPost_resultIncludesPostKey() {
        UUID postKey = UUID.randomUUID();
        RegisterPostCommand command = buildCommand(1L, null, Board.ONE_ON_ONE_INQUERY, "ORDER_PAYMENT");
        Post savedPost = buildSavedPost(101L, command, postKey);
        when(savePostPort.save(any(Post.class))).thenReturn(savedPost);

        RegisterPostResult result = registerPostService.registerPost(false, command);

        assertThat(result.postKey()).isEqualTo(postKey);
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

    @Test
    @DisplayName("상품 문의글 등록 시 제목에 욕설이 포함되면 InvalidPostContentContainsProfanityException이 발생한다")
    void registerPost_productInquiryWithProfanityInTitle_throwsException() {
        RegisterPostCommand command = RegisterPostCommand.builder()
                .userId(1L)
                .board(Board.PRODUCT_INQUERY)
                .category("PRODUCT_QUESTION")
                .writerName("작성자")
                .title("욕설제목")
                .content("정상 내용")
                .build();
        when(findProfanityWordPort.containsProfanity("욕설제목")).thenReturn(true);

        assertThatThrownBy(() -> registerPostService.registerPost(false, command))
                .isInstanceOf(InvalidPostContentContainsProfanityException.class);

        verifyNoInteractions(savePostPort);
    }

    @Test
    @DisplayName("상품 문의글 등록 시 내용에 욕설이 포함되면 InvalidPostContentContainsProfanityException이 발생한다")
    void registerPost_productInquiryWithProfanityInContent_throwsException() {
        RegisterPostCommand command = RegisterPostCommand.builder()
                .userId(1L)
                .board(Board.PRODUCT_INQUERY)
                .category("PRODUCT_QUESTION")
                .writerName("작성자")
                .title("정상 제목")
                .content("욕설내용")
                .build();
        when(findProfanityWordPort.containsProfanity("정상 제목")).thenReturn(false);
        when(findProfanityWordPort.containsProfanity("욕설내용")).thenReturn(true);

        assertThatThrownBy(() -> registerPostService.registerPost(false, command))
                .isInstanceOf(InvalidPostContentContainsProfanityException.class);

        verifyNoInteractions(savePostPort);
    }

    @Test
    @DisplayName("상품 문의글 등록 시 제목과 내용에 욕설이 없으면 정상 등록된다")
    void registerPost_productInquiryWithoutProfanity_succeeds() {
        RegisterPostCommand command = RegisterPostCommand.builder()
                .userId(1L)
                .board(Board.PRODUCT_INQUERY)
                .category("PRODUCT_QUESTION")
                .writerName("작성자")
                .title("정상 제목")
                .content("정상 내용")
                .build();
        Post savedPost = buildSavedPost(1100L, command);
        when(findProfanityWordPort.containsProfanity("정상 제목")).thenReturn(false);
        when(findProfanityWordPort.containsProfanity("정상 내용")).thenReturn(false);
        when(savePostPort.save(any(Post.class))).thenReturn(savedPost);

        RegisterPostResult result = registerPostService.registerPost(false, command);

        assertThat(result.id()).isEqualTo(1100L);
        verify(findProfanityWordPort).containsProfanity("정상 제목");
        verify(findProfanityWordPort).containsProfanity("정상 내용");
    }

    @Test
    @DisplayName("판매자 답글 등록 시 욕설 검증이 실행되지 않는다")
    void registerPost_sellerReply_skipsProfanityValidation() {
        Long sellerId = 1L;
        Long pricePolicyId = 100L;
        RegisterPostCommand command = buildReplyCommand(sellerId, 10L, pricePolicyId,
                Board.PRODUCT_INQUERY, "PRODUCT_QUESTION");
        Post savedPost = buildSavedPost(1200L, command);

        ProductInfoResult productInfo = buildProductInfo(sellerId);
        when(findProductByPricePolicyPort.findByPricePolicyIds(List.of(pricePolicyId)))
                .thenReturn(Map.of(pricePolicyId, productInfo));
        when(savePostPort.save(any(Post.class))).thenReturn(savedPost);

        RegisterPostResult result = registerPostService.registerPost(true, command);

        assertThat(result.id()).isEqualTo(1200L);
        verifyNoInteractions(findProfanityWordPort);
    }

    @Test
    @DisplayName("1:1 문의글 등록 시 욕설 검증이 실행되지 않는다")
    void registerPost_oneOnOneInquiry_skipsProfanityValidation() {
        RegisterPostCommand command = RegisterPostCommand.builder()
                .userId(1L)
                .board(Board.ONE_ON_ONE_INQUERY)
                .category("ORDER_PAYMENT")
                .writerName("작성자")
                .title("욕설포함제목")
                .content("욕설포함내용")
                .build();
        Post savedPost = buildSavedPost(1300L, command);
        when(savePostPort.save(any(Post.class))).thenReturn(savedPost);

        RegisterPostResult result = registerPostService.registerPost(false, command);

        assertThat(result.id()).isEqualTo(1300L);
        verifyNoInteractions(findProfanityWordPort);
    }

    @Test
    @DisplayName("상품 문의 답글 등록 시 부모 게시글 title이 존재하면 그대로 이벤트 title로 발행된다")
    void registerPost_productInquiryReply_parentHasTitle_publishesParentTitle() {
        Long parentId = 2000L;
        Long parentOwnerId = 50L;
        Long pricePolicyId = 500L;
        RegisterPostCommand command = buildReplyCommand(20L, parentId, pricePolicyId,
                Board.PRODUCT_INQUERY, "PRODUCT_QUESTION");
        Post parentPost = buildParentPost(parentId, parentOwnerId, Board.PRODUCT_INQUERY,
                "재입고 문의합니다", PostTargetType.PRICE_POLICY, pricePolicyId);
        Post savedPost = buildSavedPost(1400L, command);
        when(findPostPort.findById(parentId)).thenReturn(Optional.of(parentPost));
        when(savePostPort.save(any(Post.class))).thenReturn(savedPost);

        registerPostService.registerPost(false, command);

        verify(publishPostEventPort).publishInquiryAnsweredEvent(
                parentOwnerId, parentId, "재입고 문의합니다", "PRODUCT_INQUERY"
        );
        verifyNoInteractions(findProductByPricePolicyPort);
    }

    @Test
    @DisplayName("상품 문의 답글 등록 시 부모 title이 null이면 상품명으로 대체되어 이벤트가 발행된다")
    void registerPost_productInquiryReply_parentTitleNull_substitutesWithProductName() {
        Long parentId = 2001L;
        Long parentOwnerId = 51L;
        Long pricePolicyId = 501L;
        RegisterPostCommand command = buildReplyCommand(21L, parentId, pricePolicyId,
                Board.PRODUCT_INQUERY, "PRODUCT_QUESTION");
        Post parentPost = buildParentPost(parentId, parentOwnerId, Board.PRODUCT_INQUERY,
                null, PostTargetType.PRICE_POLICY, pricePolicyId);
        Post savedPost = buildSavedPost(1401L, command);
        when(findPostPort.findById(parentId)).thenReturn(Optional.of(parentPost));
        when(findProductByPricePolicyPort.findByPricePolicyIds(List.of(pricePolicyId)))
                .thenReturn(Map.of(pricePolicyId, buildProductInfo(999L, "유기농 사과 1kg")));
        when(savePostPort.save(any(Post.class))).thenReturn(savedPost);

        registerPostService.registerPost(false, command);

        verify(publishPostEventPort).publishInquiryAnsweredEvent(
                parentOwnerId, parentId, "유기농 사과 1kg", "PRODUCT_INQUERY"
        );
    }

    @Test
    @DisplayName("상품 문의 답글 등록 시 부모 title이 빈 문자열이면 상품명으로 대체되어 이벤트가 발행된다")
    void registerPost_productInquiryReply_parentTitleBlank_substitutesWithProductName() {
        Long parentId = 2002L;
        Long parentOwnerId = 52L;
        Long pricePolicyId = 502L;
        RegisterPostCommand command = buildReplyCommand(22L, parentId, pricePolicyId,
                Board.PRODUCT_INQUERY, "PRODUCT_QUESTION");
        Post parentPost = buildParentPost(parentId, parentOwnerId, Board.PRODUCT_INQUERY,
                "   ", PostTargetType.PRICE_POLICY, pricePolicyId);
        Post savedPost = buildSavedPost(1402L, command);
        when(findPostPort.findById(parentId)).thenReturn(Optional.of(parentPost));
        when(findProductByPricePolicyPort.findByPricePolicyIds(List.of(pricePolicyId)))
                .thenReturn(Map.of(pricePolicyId, buildProductInfo(999L, "테스트 상품")));
        when(savePostPort.save(any(Post.class))).thenReturn(savedPost);

        registerPostService.registerPost(false, command);

        verify(publishPostEventPort).publishInquiryAnsweredEvent(
                parentOwnerId, parentId, "테스트 상품", "PRODUCT_INQUERY"
        );
    }

    @Test
    @DisplayName("상품 문의 답글 등록 시 부모 title이 null이고 상품 조회 결과가 비어 있으면 '상품 문의' 고정값으로 이벤트가 발행된다")
    void registerPost_productInquiryReply_parentTitleNullAndProductNotFound_fallbacksToDefault() {
        Long parentId = 2003L;
        Long parentOwnerId = 53L;
        Long pricePolicyId = 503L;
        RegisterPostCommand command = buildReplyCommand(23L, parentId, pricePolicyId,
                Board.PRODUCT_INQUERY, "PRODUCT_QUESTION");
        Post parentPost = buildParentPost(parentId, parentOwnerId, Board.PRODUCT_INQUERY,
                null, PostTargetType.PRICE_POLICY, pricePolicyId);
        Post savedPost = buildSavedPost(1403L, command);
        when(findPostPort.findById(parentId)).thenReturn(Optional.of(parentPost));
        when(findProductByPricePolicyPort.findByPricePolicyIds(List.of(pricePolicyId)))
                .thenReturn(Map.of());
        when(savePostPort.save(any(Post.class))).thenReturn(savedPost);

        registerPostService.registerPost(false, command);

        verify(publishPostEventPort).publishInquiryAnsweredEvent(
                parentOwnerId, parentId, "상품 문의", "PRODUCT_INQUERY"
        );
    }

    @Test
    @DisplayName("상품 문의 답글 등록 시 조회된 상품명이 null이면 '상품 문의' 고정값으로 이벤트가 발행된다")
    void registerPost_productInquiryReply_productNameNull_fallbacksToDefault() {
        Long parentId = 2004L;
        Long parentOwnerId = 54L;
        Long pricePolicyId = 504L;
        RegisterPostCommand command = buildReplyCommand(24L, parentId, pricePolicyId,
                Board.PRODUCT_INQUERY, "PRODUCT_QUESTION");
        Post parentPost = buildParentPost(parentId, parentOwnerId, Board.PRODUCT_INQUERY,
                null, PostTargetType.PRICE_POLICY, pricePolicyId);
        Post savedPost = buildSavedPost(1404L, command);
        when(findPostPort.findById(parentId)).thenReturn(Optional.of(parentPost));
        when(findProductByPricePolicyPort.findByPricePolicyIds(List.of(pricePolicyId)))
                .thenReturn(Map.of(pricePolicyId, buildProductInfo(999L, null)));
        when(savePostPort.save(any(Post.class))).thenReturn(savedPost);

        registerPostService.registerPost(false, command);

        verify(publishPostEventPort).publishInquiryAnsweredEvent(
                parentOwnerId, parentId, "상품 문의", "PRODUCT_INQUERY"
        );
    }

    @Test
    @DisplayName("상품 문의 답글 등록 시 조회된 상품명이 빈 문자열이면 '상품 문의' 고정값으로 이벤트가 발행된다")
    void registerPost_productInquiryReply_productNameBlank_fallbacksToDefault() {
        Long parentId = 2005L;
        Long parentOwnerId = 55L;
        Long pricePolicyId = 505L;
        RegisterPostCommand command = buildReplyCommand(25L, parentId, pricePolicyId,
                Board.PRODUCT_INQUERY, "PRODUCT_QUESTION");
        Post parentPost = buildParentPost(parentId, parentOwnerId, Board.PRODUCT_INQUERY,
                null, PostTargetType.PRICE_POLICY, pricePolicyId);
        Post savedPost = buildSavedPost(1405L, command);
        when(findPostPort.findById(parentId)).thenReturn(Optional.of(parentPost));
        when(findProductByPricePolicyPort.findByPricePolicyIds(List.of(pricePolicyId)))
                .thenReturn(Map.of(pricePolicyId, buildProductInfo(999L, "  ")));
        when(savePostPort.save(any(Post.class))).thenReturn(savedPost);

        registerPostService.registerPost(false, command);

        verify(publishPostEventPort).publishInquiryAnsweredEvent(
                parentOwnerId, parentId, "상품 문의", "PRODUCT_INQUERY"
        );
    }

    @Test
    @DisplayName("상품 문의 답글 등록 시 부모 게시글 targetId가 null이면 상품 조회 없이 '상품 문의' 고정값으로 이벤트가 발행된다")
    void registerPost_productInquiryReply_parentTargetIdNull_fallbacksWithoutProductLookup() {
        Long parentId = 2006L;
        Long parentOwnerId = 56L;
        Long pricePolicyId = 506L;
        RegisterPostCommand command = buildReplyCommand(26L, parentId, pricePolicyId,
                Board.PRODUCT_INQUERY, "PRODUCT_QUESTION");
        Post parentPost = buildParentPost(parentId, parentOwnerId, Board.PRODUCT_INQUERY,
                null, PostTargetType.PRICE_POLICY, null);
        Post savedPost = buildSavedPost(1406L, command);
        when(findPostPort.findById(parentId)).thenReturn(Optional.of(parentPost));
        when(savePostPort.save(any(Post.class))).thenReturn(savedPost);

        registerPostService.registerPost(false, command);

        verify(publishPostEventPort).publishInquiryAnsweredEvent(
                parentOwnerId, parentId, "상품 문의", "PRODUCT_INQUERY"
        );
        verifyNoInteractions(findProductByPricePolicyPort);
    }

    @Test
    @DisplayName("상품 문의 답글 등록 시 부모 게시글 targetType이 null이면 상품 조회 없이 '상품 문의' 고정값으로 이벤트가 발행된다")
    void registerPost_productInquiryReply_parentTargetTypeNull_fallbacksWithoutProductLookup() {
        Long parentId = 2007L;
        Long parentOwnerId = 57L;
        Long pricePolicyId = 507L;
        RegisterPostCommand command = buildReplyCommand(27L, parentId, pricePolicyId,
                Board.PRODUCT_INQUERY, "PRODUCT_QUESTION");
        Post parentPost = buildParentPost(parentId, parentOwnerId, Board.PRODUCT_INQUERY,
                null, null, pricePolicyId);
        Post savedPost = buildSavedPost(1407L, command);
        when(findPostPort.findById(parentId)).thenReturn(Optional.of(parentPost));
        when(savePostPort.save(any(Post.class))).thenReturn(savedPost);

        registerPostService.registerPost(false, command);

        verify(publishPostEventPort).publishInquiryAnsweredEvent(
                parentOwnerId, parentId, "상품 문의", "PRODUCT_INQUERY"
        );
        verifyNoInteractions(findProductByPricePolicyPort);
    }

    @Test
    @DisplayName("1:1 문의 답글 등록 시 부모 title이 null이어도 title 그대로 이벤트가 발행된다")
    void registerPost_oneOnOneInquiryReply_parentTitleNull_publishesNullTitle() {
        Long parentId = 2008L;
        Long parentOwnerId = 58L;
        RegisterPostCommand command = RegisterPostCommand.builder()
                .userId(28L)
                .parentId(parentId)
                .board(Board.ONE_ON_ONE_INQUERY)
                .category("ORDER_PAYMENT")
                .writerName("작성자")
                .content("답변 내용입니다")
                .build();
        Post parentPost = buildParentPost(parentId, parentOwnerId, Board.ONE_ON_ONE_INQUERY,
                null, null, null);
        Post savedPost = buildSavedPost(1408L, command);
        when(findPostPort.findById(parentId)).thenReturn(Optional.of(parentPost));
        when(savePostPort.save(any(Post.class))).thenReturn(savedPost);

        registerPostService.registerPost(false, command);

        verify(publishPostEventPort).publishInquiryAnsweredEvent(
                parentOwnerId, parentId, null, "ONE_ON_ONE_INQUERY"
        );
        verifyNoInteractions(findProductByPricePolicyPort);
    }

    @Test
    @DisplayName("상품 문의 답글 이벤트의 board 인자는 PRODUCT_INQUERY 문자열로 전달된다")
    void registerPost_productInquiryReply_passesProductInqueryBoardName() {
        Long parentId = 2009L;
        Long parentOwnerId = 59L;
        Long pricePolicyId = 509L;
        RegisterPostCommand command = buildReplyCommand(29L, parentId, pricePolicyId,
                Board.PRODUCT_INQUERY, "PRODUCT_QUESTION");
        Post parentPost = buildParentPost(parentId, parentOwnerId, Board.PRODUCT_INQUERY,
                "정상 제목", PostTargetType.PRICE_POLICY, pricePolicyId);
        Post savedPost = buildSavedPost(1409L, command);
        when(findPostPort.findById(parentId)).thenReturn(Optional.of(parentPost));
        when(savePostPort.save(any(Post.class))).thenReturn(savedPost);

        registerPostService.registerPost(false, command);

        verify(publishPostEventPort).publishInquiryAnsweredEvent(
                parentOwnerId, parentId, "정상 제목", "PRODUCT_INQUERY"
        );
    }

    @Test
    @DisplayName("상품 문의 답글 등록 시 부모 게시글 board가 PRODUCT_INQUERY가 아니면 이벤트 발행을 skip한다")
    void registerPost_productInquiryReply_parentBoardMismatch_skipsEventPublish() {
        Long parentId = 3000L;
        Long parentOwnerId = 80L;
        Long pricePolicyId = 600L;
        RegisterPostCommand command = buildReplyCommand(45L, parentId, pricePolicyId,
                Board.PRODUCT_INQUERY, "PRODUCT_QUESTION");
        Post parentPost = buildParentPost(parentId, parentOwnerId, Board.ONE_ON_ONE_INQUERY,
                "1:1 문의 제목", null, null);
        Post savedPost = buildSavedPost(1600L, command);
        when(findPostPort.findById(parentId)).thenReturn(Optional.of(parentPost));
        when(savePostPort.save(any(Post.class))).thenReturn(savedPost);

        registerPostService.registerPost(false, command);

        verify(publishPostEventPort, never()).publishInquiryAnsweredEvent(
                any(), any(), any(), any()
        );
        verifyNoInteractions(findProductByPricePolicyPort);
    }

    @Test
    @DisplayName("1:1 문의 답글 등록 시 부모 게시글 board가 ONE_ON_ONE_INQUERY가 아니면 이벤트 발행을 skip한다")
    void registerPost_oneOnOneInquiryReply_parentBoardMismatch_skipsEventPublish() {
        Long parentId = 3001L;
        Long parentOwnerId = 81L;
        RegisterPostCommand command = RegisterPostCommand.builder()
                .userId(46L)
                .parentId(parentId)
                .board(Board.ONE_ON_ONE_INQUERY)
                .category("ORDER_PAYMENT")
                .writerName("작성자")
                .content("답변 내용")
                .build();
        Post parentPost = buildParentPost(parentId, parentOwnerId, Board.PRODUCT_INQUERY,
                "상품 문의 제목", PostTargetType.PRICE_POLICY, 700L);
        Post savedPost = buildSavedPost(1601L, command);
        when(findPostPort.findById(parentId)).thenReturn(Optional.of(parentPost));
        when(savePostPort.save(any(Post.class))).thenReturn(savedPost);

        registerPostService.registerPost(false, command);

        verify(publishPostEventPort, never()).publishInquiryAnsweredEvent(
                any(), any(), any(), any()
        );
        verifyNoInteractions(findProductByPricePolicyPort);
    }

    @Test
    @DisplayName("부모 게시글 조회 실패 시 상품 문의 답글 이벤트는 발행되지 않는다")
    void registerPost_productInquiryReply_parentNotFound_doesNotPublishEvent() {
        Long parentId = 2010L;
        Long pricePolicyId = 510L;
        RegisterPostCommand command = buildReplyCommand(30L, parentId, pricePolicyId,
                Board.PRODUCT_INQUERY, "PRODUCT_QUESTION");
        Post savedPost = buildSavedPost(1410L, command);
        when(findPostPort.findById(parentId)).thenReturn(Optional.empty());
        when(savePostPort.save(any(Post.class))).thenReturn(savedPost);

        registerPostService.registerPost(false, command);

        verifyNoInteractions(publishPostEventPort);
        verifyNoInteractions(findProductByPricePolicyPort);
    }

    @Test
    @DisplayName("상품 문의 답글 등록 시 조회된 상품명에 중괄호가 포함되면 제거한 후 이벤트 title로 발행된다")
    void registerPost_productInquiryReply_productNameContainsBraces_removesBraces() {
        Long parentId = 2020L;
        Long parentOwnerId = 70L;
        Long pricePolicyId = 520L;
        RegisterPostCommand command = buildReplyCommand(40L, parentId, pricePolicyId,
                Board.PRODUCT_INQUERY, "PRODUCT_QUESTION");
        Post parentPost = buildParentPost(parentId, parentOwnerId, Board.PRODUCT_INQUERY,
                null, PostTargetType.PRICE_POLICY, pricePolicyId);
        Post savedPost = buildSavedPost(1500L, command);
        when(findPostPort.findById(parentId)).thenReturn(Optional.of(parentPost));
        when(findProductByPricePolicyPort.findByPricePolicyIds(List.of(pricePolicyId)))
                .thenReturn(Map.of(pricePolicyId, buildProductInfo(999L, "유기농 사과 {post_id}")));
        when(savePostPort.save(any(Post.class))).thenReturn(savedPost);

        registerPostService.registerPost(false, command);

        verify(publishPostEventPort).publishInquiryAnsweredEvent(
                parentOwnerId, parentId, "유기농 사과 post_id", "PRODUCT_INQUERY"
        );
    }

    @Test
    @DisplayName("상품 문의 답글 등록 시 조회된 상품명이 200자를 초과하면 앞 200자로 잘려 이벤트 title로 발행된다")
    void registerPost_productInquiryReply_productNameExceedsMaxLength_truncatesTo200Chars() {
        Long parentId = 2021L;
        Long parentOwnerId = 71L;
        Long pricePolicyId = 521L;
        RegisterPostCommand command = buildReplyCommand(41L, parentId, pricePolicyId,
                Board.PRODUCT_INQUERY, "PRODUCT_QUESTION");
        Post parentPost = buildParentPost(parentId, parentOwnerId, Board.PRODUCT_INQUERY,
                null, PostTargetType.PRICE_POLICY, pricePolicyId);
        Post savedPost = buildSavedPost(1501L, command);
        String longName = "가".repeat(250);
        String expectedTitle = "가".repeat(200);
        when(findPostPort.findById(parentId)).thenReturn(Optional.of(parentPost));
        when(findProductByPricePolicyPort.findByPricePolicyIds(List.of(pricePolicyId)))
                .thenReturn(Map.of(pricePolicyId, buildProductInfo(999L, longName)));
        when(savePostPort.save(any(Post.class))).thenReturn(savedPost);

        registerPostService.registerPost(false, command);

        verify(publishPostEventPort).publishInquiryAnsweredEvent(
                parentOwnerId, parentId, expectedTitle, "PRODUCT_INQUERY"
        );
    }

    @Test
    @DisplayName("상품 문의 답글 등록 시 조회된 상품명이 정확히 200자면 그대로 이벤트 title로 발행된다")
    void registerPost_productInquiryReply_productNameExactly200Chars_keepsOriginal() {
        Long parentId = 2022L;
        Long parentOwnerId = 72L;
        Long pricePolicyId = 522L;
        RegisterPostCommand command = buildReplyCommand(42L, parentId, pricePolicyId,
                Board.PRODUCT_INQUERY, "PRODUCT_QUESTION");
        Post parentPost = buildParentPost(parentId, parentOwnerId, Board.PRODUCT_INQUERY,
                null, PostTargetType.PRICE_POLICY, pricePolicyId);
        Post savedPost = buildSavedPost(1502L, command);
        String exactName = "나".repeat(200);
        when(findPostPort.findById(parentId)).thenReturn(Optional.of(parentPost));
        when(findProductByPricePolicyPort.findByPricePolicyIds(List.of(pricePolicyId)))
                .thenReturn(Map.of(pricePolicyId, buildProductInfo(999L, exactName)));
        when(savePostPort.save(any(Post.class))).thenReturn(savedPost);

        registerPostService.registerPost(false, command);

        verify(publishPostEventPort).publishInquiryAnsweredEvent(
                parentOwnerId, parentId, exactName, "PRODUCT_INQUERY"
        );
    }

    @Test
    @DisplayName("판매자 답글 등록 시 부모 title이 null이면 상품명으로 대체되어 이벤트가 발행된다")
    void registerPost_sellerProductInquiryReply_parentTitleNull_substitutesWithProductName() {
        Long sellerId = 60L;
        Long parentId = 2011L;
        Long parentOwnerId = 61L;
        Long pricePolicyId = 511L;
        RegisterPostCommand command = buildReplyCommand(sellerId, parentId, pricePolicyId,
                Board.PRODUCT_INQUERY, "PRODUCT_QUESTION");
        Post parentPost = buildParentPost(parentId, parentOwnerId, Board.PRODUCT_INQUERY,
                null, PostTargetType.PRICE_POLICY, pricePolicyId);
        Post savedPost = buildSavedPost(1411L, command);
        when(findProductByPricePolicyPort.findByPricePolicyIds(List.of(pricePolicyId)))
                .thenReturn(Map.of(pricePolicyId, buildProductInfo(sellerId, "판매자상품")));
        when(findPostPort.findById(parentId)).thenReturn(Optional.of(parentPost));
        when(savePostPort.save(any(Post.class))).thenReturn(savedPost);

        registerPostService.registerPost(true, command);

        verify(publishPostEventPort).publishInquiryAnsweredEvent(
                parentOwnerId, parentId, "판매자상품", "PRODUCT_INQUERY"
        );
        verify(findProductByPricePolicyPort, times(1))
                .findByPricePolicyIds(List.of(pricePolicyId));
    }

    @Test
    @DisplayName("판매자 답글 등록 시 부모 post targetId가 판매자 검증 pricePolicyId와 다르면 신규 조회한다")
    void registerPost_sellerProductInquiryReply_parentTargetIdDiffers_performsSecondLookup() {
        Long sellerId = 62L;
        Long parentId = 2030L;
        Long parentOwnerId = 63L;
        Long sellerPricePolicyId = 530L;
        Long parentPricePolicyId = 531L;
        RegisterPostCommand command = buildReplyCommand(sellerId, parentId, sellerPricePolicyId,
                Board.PRODUCT_INQUERY, "PRODUCT_QUESTION");
        Post parentPost = buildParentPost(parentId, parentOwnerId, Board.PRODUCT_INQUERY,
                null, PostTargetType.PRICE_POLICY, parentPricePolicyId);
        Post savedPost = buildSavedPost(1700L, command);
        when(findProductByPricePolicyPort.findByPricePolicyIds(List.of(sellerPricePolicyId)))
                .thenReturn(Map.of(sellerPricePolicyId, buildProductInfo(sellerId, "판매자상품")));
        when(findProductByPricePolicyPort.findByPricePolicyIds(List.of(parentPricePolicyId)))
                .thenReturn(Map.of(parentPricePolicyId, buildProductInfo(999L, "부모상품")));
        when(findPostPort.findById(parentId)).thenReturn(Optional.of(parentPost));
        when(savePostPort.save(any(Post.class))).thenReturn(savedPost);

        registerPostService.registerPost(true, command);

        verify(publishPostEventPort).publishInquiryAnsweredEvent(
                parentOwnerId, parentId, "부모상품", "PRODUCT_INQUERY"
        );
        verify(findProductByPricePolicyPort).findByPricePolicyIds(List.of(sellerPricePolicyId));
        verify(findProductByPricePolicyPort).findByPricePolicyIds(List.of(parentPricePolicyId));
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
        return buildSavedPost(id, command, UUID.randomUUID());
    }

    private Post buildSavedPost(Long id, RegisterPostCommand command, UUID postKey) {
        String maskedName = command.board().requiresWriterMasking()
                ? ValueMasker.mask(command.writerName())
                : command.writerName();
        return Post.from(
                PostSnapshotState.builder()
                        .id(id)
                        .userId(command.userId())
                        .postKey(postKey)
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

    private ProductInfoResult buildProductInfo(Long sellerId, String name) {
        return new ProductInfoResult(sellerId, name, "브랜드명", null, List.of(), null);
    }

    private Post buildParentPost(
            Long id, Long userId, Board board, String title,
            PostTargetType targetType, Long targetId
    ) {
        String category = board.isOneOnOneInquery() ? "ORDER_PAYMENT" : "PRODUCT_QUESTION";
        return Post.from(
                PostSnapshotState.builder()
                        .id(id)
                        .userId(userId)
                        .postKey(UUID.randomUUID())
                        .board(board)
                        .category(category)
                        .targetType(targetType)
                        .targetId(targetId)
                        .writerName("부모작성자")
                        .maskedWriterName("부모작성자")
                        .title(title)
                        .content("부모 게시글 내용")
                        .isPrivate(false)
                        .isPhoto(false)
                        .status(EntityStatus.ACTIVE)
                        .createdAt(LocalDateTime.now())
                        .modifiedAt(LocalDateTime.now())
                        .orderNum(id)
                        .build()
        );
    }
}
