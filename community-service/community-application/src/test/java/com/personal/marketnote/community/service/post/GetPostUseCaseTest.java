package com.personal.marketnote.community.service.post;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.common.application.file.port.in.result.GetFileResult;
import com.personal.marketnote.common.application.file.port.in.result.GetFilesResult;
import com.personal.marketnote.community.domain.post.Board;
import com.personal.marketnote.community.domain.post.Post;
import com.personal.marketnote.community.domain.post.PostSnapshotState;
import com.personal.marketnote.community.domain.post.PostTargetType;
import com.personal.marketnote.community.exception.PostBoardMismatchException;
import com.personal.marketnote.community.exception.PostNotFoundException;
import com.personal.marketnote.community.port.in.command.post.GetPostQuery;
import com.personal.marketnote.community.port.in.result.post.PostItemResult;
import com.personal.marketnote.community.port.out.file.FindPostImagesPort;
import com.personal.marketnote.community.port.out.post.FindPostPort;
import com.personal.marketnote.community.port.out.product.FindProductByPricePolicyPort;
import com.personal.marketnote.community.port.out.result.product.ProductInfoResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.personal.marketnote.common.domain.file.FileSort.POST_IMAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetPostUseCaseTest {
    @Mock
    private FindPostPort findPostPort;
    @Mock
    private FindProductByPricePolicyPort findProductByPricePolicyPort;
    @Mock
    private FindPostImagesPort findPostImagesPort;

    @InjectMocks
    private GetPostService getPostService;

    // ========== A. 기본 조회 및 검증 ==========

    @Test
    @DisplayName("게시글이 존재하면 PostItemResult를 반환한다")
    void getPost_postExists_returnsPostItemResult() {
        Long postId = 1L;
        Post post = buildOneOnOnePost(postId, 1L);
        mockFindByIdWithReplies(postId, post);
        GetPostQuery query = buildQuery(Board.ONE_ON_ONE_INQUERY, null, 1L, postId);

        PostItemResult result = getPostService.getPost(query);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(postId);
    }

    @Test
    @DisplayName("게시글이 존재하지 않으면 PostNotFoundException이 발생한다")
    void getPost_postNotExists_throwsPostNotFoundException() {
        Long postId = 999L;
        when(findPostPort.findByIdWithReplies(postId)).thenReturn(Optional.empty());
        GetPostQuery query = buildQuery(Board.ONE_ON_ONE_INQUERY, null, 1L, postId);

        assertThatThrownBy(() -> getPostService.getPost(query))
                .isInstanceOf(PostNotFoundException.class);
    }

    @Test
    @DisplayName("PostNotFoundException 메시지에 게시글 ID가 포함된다")
    void getPost_postNotFound_exceptionContainsPostId() {
        Long postId = 999L;
        when(findPostPort.findByIdWithReplies(postId)).thenReturn(Optional.empty());
        GetPostQuery query = buildQuery(Board.ONE_ON_ONE_INQUERY, null, 1L, postId);

        assertThatThrownBy(() -> getPostService.getPost(query))
                .isInstanceOf(PostNotFoundException.class)
                .hasMessageContaining(String.valueOf(postId));
    }

    @Test
    @DisplayName("요청한 게시판과 게시글의 게시판이 일치하지 않으면 PostBoardMismatchException이 발생한다")
    void getPost_boardMismatch_throwsPostBoardMismatchException() {
        Long postId = 1L;
        Post post = buildOneOnOnePost(postId, 1L);
        mockFindByIdWithReplies(postId, post);
        GetPostQuery query = buildQuery(Board.NOTICE, null, 1L, postId);

        assertThatThrownBy(() -> getPostService.getPost(query))
                .isInstanceOf(PostBoardMismatchException.class)
                .hasMessageContaining("게시판");
    }

    // ========== B. targetId 없는 게시글 ==========

    @Test
    @DisplayName("targetId가 없는 게시글이면 상품 정보 없이 반환된다")
    void getPost_noTargetId_returnsWithoutProductInfo() {
        Long postId = 1L;
        Post post = buildOneOnOnePost(postId, 1L);
        mockFindByIdWithReplies(postId, post);
        GetPostQuery query = buildQuery(Board.ONE_ON_ONE_INQUERY, null, 1L, postId);

        PostItemResult result = getPostService.getPost(query);

        assertThat(result.getProduct()).isNull();
    }

    @Test
    @DisplayName("targetId가 없는 게시글이면 답글이 항상 추가된다")
    void getPost_noTargetId_repliesAlwaysAdded() {
        Long postId = 1L;
        Post post = buildOneOnOnePost(postId, 1L);
        Post reply = buildOneOnOnePost(2L, 1L);
        post.addReplies(List.of(reply));
        mockFindByIdWithReplies(postId, post);
        GetPostQuery query = buildQuery(Board.ONE_ON_ONE_INQUERY, null, 1L, postId);

        PostItemResult result = getPostService.getPost(query);

        assertThat(result.getReplies()).isNotNull();
        assertThat(result.getReplies()).hasSize(1);
        assertThat(result.getReplies().getFirst().getId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("targetId가 없는 게시글이면 FindProductByPricePolicyPort가 호출되지 않는다")
    void getPost_noTargetId_doesNotCallProductPort() {
        Long postId = 1L;
        Post post = buildOneOnOnePost(postId, 1L);
        mockFindByIdWithReplies(postId, post);
        GetPostQuery query = buildQuery(Board.ONE_ON_ONE_INQUERY, null, 1L, postId);

        getPostService.getPost(query);

        verifyNoInteractions(findProductByPricePolicyPort);
    }

    // ========== C. 상품 정보 조회 ==========

    @Test
    @DisplayName("PRODUCT_INQUERY + PRICE_POLICY targetType이면 상품 정보가 포함된다")
    void getPost_productInqueryWithPricePolicy_includesProductInfo() {
        Long postId = 1L;
        Long targetId = 100L;
        Post post = buildProductInqueryPost(postId, 1L, targetId, false);
        mockFindByIdWithReplies(postId, post);
        ProductInfoResult productInfo = buildProductInfo(1L);
        when(findProductByPricePolicyPort.findByPricePolicyIds(List.of(targetId)))
                .thenReturn(Map.of(targetId, productInfo));
        GetPostQuery query = buildQuery(Board.PRODUCT_INQUERY, null, 1L, postId);

        PostItemResult result = getPostService.getPost(query);

        assertThat(result.getProduct()).isNotNull();
        assertThat(result.getProduct().name()).isEqualTo("상품명");
        assertThat(result.getProduct().brandName()).isEqualTo("브랜드명");
    }

    @Test
    @DisplayName("PRODUCT_INQUERY가 아닌 게시판이면 상품 정보가 조회되지 않는다")
    void getPost_nonProductInqueryBoard_doesNotLookUpProductInfo() {
        Long postId = 1L;
        Post post = buildOneOnOnePostWithTargetId(postId, 1L, 100L);
        mockFindByIdWithReplies(postId, post);
        GetPostQuery query = buildQuery(Board.ONE_ON_ONE_INQUERY, null, 1L, postId);

        PostItemResult result = getPostService.getPost(query);

        verifyNoInteractions(findProductByPricePolicyPort);
        assertThat(result.getProduct()).isNull();
    }

    @Test
    @DisplayName("PRODUCT_INQUERY이지만 게시글의 targetType이 PRICE_POLICY가 아니면 상품 정보가 조회되지 않는다")
    void getPost_productInquery_nonPricePolicyTargetType_doesNotLookUpProductInfo() {
        Long postId = 1L;
        Post post = buildProductInqueryPostWithNullTargetType(postId, 1L, 100L);
        mockFindByIdWithReplies(postId, post);
        GetPostQuery query = buildQuery(Board.PRODUCT_INQUERY, null, 1L, postId);

        PostItemResult result = getPostService.getPost(query);

        verifyNoInteractions(findProductByPricePolicyPort);
        assertThat(result.getProduct()).isNull();
    }

    @Test
    @DisplayName("상품 정보 조회 결과에 해당 targetId가 없으면 상품 정보가 null이다")
    void getPost_productInfoNotFound_productIsNull() {
        Long postId = 1L;
        Long targetId = 100L;
        Post post = buildProductInqueryPost(postId, 1L, targetId, false);
        mockFindByIdWithReplies(postId, post);
        when(findProductByPricePolicyPort.findByPricePolicyIds(List.of(targetId)))
                .thenReturn(Map.of());
        GetPostQuery query = buildQuery(Board.PRODUCT_INQUERY, null, 1L, postId);

        PostItemResult result = getPostService.getPost(query);

        assertThat(result.getProduct()).isNull();
    }

    // ========== D. 비밀글 마스킹 ==========

    @Test
    @DisplayName("query.targetType 있고 비밀글이고 다른 사용자이고 관리자/판매자 아니면 마스킹된다")
    void getPost_privatePost_differentUser_notAdmin_masked() {
        Long postId = 1L;
        Long targetId = 100L;
        Post post = buildProductInqueryPost(postId, 999L, targetId, true);
        mockFindByIdWithReplies(postId, post);
        mockProductInfoForTarget(targetId, 777L);
        GetPostQuery query = buildQuery(Board.PRODUCT_INQUERY, PostTargetType.PRICE_POLICY, 1L, postId);

        PostItemResult result = getPostService.getPost(query);

        assertThat(result.isMasked()).isTrue();
        assertThat(result.getTitle()).isNull();
        assertThat(result.getContent()).isNull();
    }

    @Test
    @DisplayName("query.targetType 있고 비밀글이지만 본인 게시글이면 마스킹되지 않는다")
    void getPost_privatePost_sameUser_notMasked() {
        Long postId = 1L;
        Long userId = 1L;
        Long targetId = 100L;
        Post post = buildProductInqueryPost(postId, userId, targetId, true);
        mockFindByIdWithReplies(postId, post);
        mockProductInfoForTarget(targetId, 777L);
        GetPostQuery query = buildQuery(Board.PRODUCT_INQUERY, PostTargetType.PRICE_POLICY, userId, postId);

        PostItemResult result = getPostService.getPost(query);

        assertThat(result.isMasked()).isFalse();
        assertThat(result.getTitle()).isNotNull();
    }

    @Test
    @DisplayName("query.targetType 없으면 비밀글이어도 마스킹되지 않는다")
    void getPost_noTargetType_privatePost_notMasked() {
        Long postId = 1L;
        Long targetId = 100L;
        Post post = buildProductInqueryPost(postId, 999L, targetId, true);
        mockFindByIdWithReplies(postId, post);
        mockProductInfoForTarget(targetId, 777L);
        GetPostQuery query = buildQuery(Board.PRODUCT_INQUERY, null, 1L, postId);

        PostItemResult result = getPostService.getPost(query);

        assertThat(result.isMasked()).isFalse();
    }

    @Test
    @DisplayName("query.targetType 있고 관리자이면 비밀글이어도 마스킹되지 않는다")
    void getPost_adminUser_privatePost_notMasked() {
        Long postId = 1L;
        Long targetId = 100L;
        Post post = buildProductInqueryPost(postId, 999L, targetId, true);
        mockFindByIdWithReplies(postId, post);
        mockProductInfoForTarget(targetId, 777L);
        OAuth2AuthenticatedPrincipal adminPrincipal = buildAdminPrincipal();
        GetPostQuery query = GetPostQuery.builder()
                .principal(adminPrincipal)
                .userId(1L)
                .board(Board.PRODUCT_INQUERY)
                .targetType(PostTargetType.PRICE_POLICY)
                .id(postId)
                .build();

        PostItemResult result = getPostService.getPost(query);

        assertThat(result.isMasked()).isFalse();
        assertThat(result.getTitle()).isNotNull();
    }

    // ========== E. 답글 (targetId 있는 게시글) ==========

    @Test
    @DisplayName("마스킹되지 않고 답글이 있으면 답글이 추가된다")
    void getPost_notMasked_hasReplies_repliesAdded() {
        Long postId = 1L;
        Long userId = 1L;
        Long targetId = 100L;
        Post parentPost = buildProductInqueryPost(postId, userId, targetId, false);
        Post replyPost = buildProductInqueryPost(2L, userId, targetId, false);
        parentPost.addReplies(List.of(replyPost));
        mockFindByIdWithReplies(postId, parentPost);
        mockProductInfoForTarget(targetId, userId);
        GetPostQuery query = buildQuery(Board.PRODUCT_INQUERY, PostTargetType.PRICE_POLICY, userId, postId);

        PostItemResult result = getPostService.getPost(query);

        assertThat(result.isMasked()).isFalse();
        assertThat(result.getReplies()).isNotNull();
        assertThat(result.getReplies()).hasSize(1);
        assertThat(result.getReplies().getFirst().getId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("마스킹되면 답글이 추가되지 않는다")
    void getPost_masked_repliesNotAdded() {
        Long postId = 1L;
        Long targetId = 100L;
        Post parentPost = buildProductInqueryPost(postId, 999L, targetId, true);
        Post replyPost = buildProductInqueryPost(2L, 999L, targetId, false);
        parentPost.addReplies(List.of(replyPost));
        mockFindByIdWithReplies(postId, parentPost);
        mockProductInfoForTarget(targetId, 777L);
        GetPostQuery query = buildQuery(Board.PRODUCT_INQUERY, PostTargetType.PRICE_POLICY, 1L, postId);

        PostItemResult result = getPostService.getPost(query);

        assertThat(result.isMasked()).isTrue();
        assertThat(result.getReplies()).isNull();
    }

    @Test
    @DisplayName("답글이 없으면 답글 목록이 null이다")
    void getPost_noReplies_repliesNull() {
        Long postId = 1L;
        Long userId = 1L;
        Long targetId = 100L;
        Post post = buildProductInqueryPost(postId, userId, targetId, false);
        mockFindByIdWithReplies(postId, post);
        mockProductInfoForTarget(targetId, userId);
        GetPostQuery query = buildQuery(Board.PRODUCT_INQUERY, PostTargetType.PRICE_POLICY, userId, postId);

        PostItemResult result = getPostService.getPost(query);

        assertThat(result.getReplies()).isNull();
    }

    // ========== F. 이미지 ==========

    @Test
    @DisplayName("사진 게시글이면 이미지가 PostItemResult에 포함된다")
    void getPost_photoPost_includesImages() {
        Long postId = 1L;
        Post photoPost = buildPhotoPost(postId, Board.ONE_ON_ONE_INQUERY, "ORDER_PAYMENT");
        mockFindByIdWithReplies(postId, photoPost);
        GetFileResult fileResult = new GetFileResult(
                1L, "POST_IMAGE", "jpg", "image.jpg", "https://s3/image.jpg", List.of(), 1L
        );
        when(findPostImagesPort.findImagesByPostIdAndSort(eq(postId), eq(POST_IMAGE)))
                .thenReturn(Optional.of(new GetFilesResult(List.of(fileResult))));
        GetPostQuery query = buildQuery(Board.ONE_ON_ONE_INQUERY, null, 1L, postId);

        PostItemResult result = getPostService.getPost(query);

        assertThat(result.getImages()).isNotNull();
        assertThat(result.getImages()).hasSize(1);
    }

    @Test
    @DisplayName("사진 게시글이 아니면 FindPostImagesPort가 호출되지 않는다")
    void getPost_nonPhotoPost_doesNotCallImagePort() {
        Long postId = 1L;
        Post post = buildOneOnOnePost(postId, 1L);
        mockFindByIdWithReplies(postId, post);
        GetPostQuery query = buildQuery(Board.ONE_ON_ONE_INQUERY, null, 1L, postId);

        getPostService.getPost(query);

        verifyNoInteractions(findPostImagesPort);
    }

    // ========== G. 예외 전파 ==========

    @Test
    @DisplayName("findByIdWithReplies 실행 중 예외가 발생하면 전파된다")
    void getPost_findByIdWithRepliesThrows_propagatesException() {
        Long postId = 1L;
        RuntimeException exception = new RuntimeException("database error");
        when(findPostPort.findByIdWithReplies(postId)).thenThrow(exception);
        GetPostQuery query = buildQuery(Board.ONE_ON_ONE_INQUERY, null, 1L, postId);

        assertThatThrownBy(() -> getPostService.getPost(query))
                .isSameAs(exception);
    }

    @Test
    @DisplayName("findProductByPricePolicyPort 실행 중 예외가 발생하면 전파된다")
    void getPost_productPortThrows_propagatesException() {
        Long postId = 1L;
        Long targetId = 100L;
        Post post = buildProductInqueryPost(postId, 1L, targetId, false);
        mockFindByIdWithReplies(postId, post);
        RuntimeException exception = new RuntimeException("product service error");
        when(findProductByPricePolicyPort.findByPricePolicyIds(List.of(targetId)))
                .thenThrow(exception);
        GetPostQuery query = buildQuery(Board.PRODUCT_INQUERY, null, 1L, postId);

        assertThatThrownBy(() -> getPostService.getPost(query))
                .isSameAs(exception);
    }

    // ========== Query Builder ==========

    private GetPostQuery buildQuery(Board board, PostTargetType targetType, Long userId, Long postId) {
        return GetPostQuery.builder()
                .principal(null)
                .userId(userId)
                .board(board)
                .targetType(targetType)
                .id(postId)
                .build();
    }

    // ========== Post Builders ==========

    private Post buildOneOnOnePost(Long id, Long userId) {
        return Post.from(PostSnapshotState.builder()
                .id(id)
                .userId(userId)
                .board(Board.ONE_ON_ONE_INQUERY)
                .category("ORDER_PAYMENT")
                .title("문의 제목")
                .content("문의 내용")
                .status(EntityStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .orderNum(id)
                .build());
    }

    private Post buildOneOnOnePostWithTargetId(Long id, Long userId, Long targetId) {
        return Post.from(PostSnapshotState.builder()
                .id(id)
                .userId(userId)
                .board(Board.ONE_ON_ONE_INQUERY)
                .category("ORDER_PAYMENT")
                .targetType(PostTargetType.PRICE_POLICY)
                .targetId(targetId)
                .title("문의 제목")
                .content("문의 내용")
                .status(EntityStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .orderNum(id)
                .build());
    }

    private Post buildProductInqueryPost(Long id, Long userId, Long targetId, boolean isPrivate) {
        return Post.from(PostSnapshotState.builder()
                .id(id)
                .userId(userId)
                .board(Board.PRODUCT_INQUERY)
                .category("PRODUCT_QUESTION")
                .targetType(PostTargetType.PRICE_POLICY)
                .targetId(targetId)
                .title("상품 문의 제목")
                .content("상품 문의 내용")
                .isPrivate(isPrivate)
                .status(EntityStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .orderNum(id)
                .build());
    }

    private Post buildProductInqueryPostWithNullTargetType(Long id, Long userId, Long targetId) {
        return Post.from(PostSnapshotState.builder()
                .id(id)
                .userId(userId)
                .board(Board.PRODUCT_INQUERY)
                .category("PRODUCT_QUESTION")
                .targetType(null)
                .targetId(targetId)
                .title("상품 문의 제목")
                .content("상품 문의 내용")
                .status(EntityStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .orderNum(id)
                .build());
    }

    private Post buildPhotoPost(Long id, Board board, String category) {
        return Post.from(PostSnapshotState.builder()
                .id(id)
                .userId(1L)
                .board(board)
                .category(category)
                .title("사진 게시글")
                .content("내용")
                .isPhoto(true)
                .status(EntityStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .orderNum(id)
                .build());
    }

    private ProductInfoResult buildProductInfo(Long sellerId) {
        return new ProductInfoResult(sellerId, "상품명", "브랜드명", null, List.of(), null);
    }

    // ========== Mock Helpers ==========

    private void mockFindByIdWithReplies(Long id, Post post) {
        when(findPostPort.findByIdWithReplies(id)).thenReturn(Optional.of(post));
    }

    private void mockProductInfoForTarget(Long targetId, Long sellerId) {
        ProductInfoResult productInfo = buildProductInfo(sellerId);
        when(findProductByPricePolicyPort.findByPricePolicyIds(List.of(targetId)))
                .thenReturn(Map.of(targetId, productInfo));
    }

    @SuppressWarnings("unchecked")
    private OAuth2AuthenticatedPrincipal buildAdminPrincipal() {
        OAuth2AuthenticatedPrincipal principal = mock(OAuth2AuthenticatedPrincipal.class);
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .when(principal).getAuthorities();
        return principal;
    }
}
