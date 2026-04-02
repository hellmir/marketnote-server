package com.personal.marketnote.community.service.post;

import com.personal.marketnote.common.adapter.out.persistence.audit.EntityStatus;
import com.personal.marketnote.common.application.file.port.in.result.GetFileResult;
import com.personal.marketnote.common.application.file.port.in.result.GetFilesResult;
import com.personal.marketnote.community.domain.post.*;
import com.personal.marketnote.community.port.in.command.post.GetUserProductInquiryPostsCommand;
import com.personal.marketnote.community.port.in.result.post.GetUserProductInquiryPostsResult;
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
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.personal.marketnote.common.domain.file.FileSort.POST_IMAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetUserProductInquiryPostsUseCaseTest {
    @Mock
    private FindPostPort findPostPort;
    @Mock
    private FindProductByPricePolicyPort findProductByPricePolicyPort;
    @Mock
    private FindPostImagesPort findPostImagesPort;

    @InjectMocks
    private GetUserProductInquiryPostsService getUserProductInquiryPostsService;

    // ========== A. 기본 조회 ==========

    @Test
    @DisplayName("회원의 상품 문의 내역을 오프셋 기반으로 조회한다")
    void getUserProductInquiryPosts_returnsOffsetBasedResult() {
        // given
        Long userId = 1L;
        GetUserProductInquiryPostsCommand command = new GetUserProductInquiryPostsCommand(
                userId, 1, 10, Sort.Direction.DESC, PostSortProperty.ID
        );
        Posts posts = Posts.from(List.of(
                buildProductInquiryPost(1L, userId, 100L),
                buildProductInquiryPost(2L, userId, 200L)
        ));
        mockFindUserPostsByOffset(posts);
        when(findPostPort.countUserPosts(userId, Board.PRODUCT_INQUERY, null, null)).thenReturn(2L);
        mockProductInfoPort(Map.of());

        // when
        GetUserProductInquiryPostsResult result = getUserProductInquiryPostsService.getUserProductInquiryPosts(command);

        // then
        assertThat(result.page()).isEqualTo(1);
        assertThat(result.pageSize()).isEqualTo(10);
        assertThat(result.totalElements()).isEqualTo(2L);
        assertThat(result.totalPages()).isEqualTo(1);
        assertThat(result.posts()).hasSize(2);
    }

    @Test
    @DisplayName("조회 결과가 비어 있으면 빈 목록과 총 페이지 0을 반환한다")
    void getUserProductInquiryPosts_emptyResult_returnsEmptyListAndZeroPages() {
        // given
        GetUserProductInquiryPostsCommand command = new GetUserProductInquiryPostsCommand(
                1L, 1, 10, Sort.Direction.DESC, PostSortProperty.ID
        );
        mockFindUserPostsByOffset(emptyPosts());
        when(findPostPort.countUserPosts(1L, Board.PRODUCT_INQUERY, null, null)).thenReturn(0L);

        // when
        GetUserProductInquiryPostsResult result = getUserProductInquiryPostsService.getUserProductInquiryPosts(command);

        // then
        assertThat(result.posts()).isEmpty();
        assertThat(result.totalElements()).isZero();
        assertThat(result.totalPages()).isZero();
    }

    // ========== B. 페이지네이션 ==========

    @Test
    @DisplayName("totalPages가 올바르게 계산된다 (올림)")
    void getUserProductInquiryPosts_totalPagesComputedCorrectly() {
        // given
        GetUserProductInquiryPostsCommand command = new GetUserProductInquiryPostsCommand(
                1L, 1, 3, Sort.Direction.DESC, PostSortProperty.ID
        );
        Posts posts = Posts.from(List.of(
                buildProductInquiryPost(1L, 1L, 100L),
                buildProductInquiryPost(2L, 1L, 200L),
                buildProductInquiryPost(3L, 1L, 300L)
        ));
        mockFindUserPostsByOffset(posts);
        when(findPostPort.countUserPosts(1L, Board.PRODUCT_INQUERY, null, null)).thenReturn(7L);
        mockProductInfoPort(Map.of());

        // when
        GetUserProductInquiryPostsResult result = getUserProductInquiryPostsService.getUserProductInquiryPosts(command);

        // then
        assertThat(result.totalPages()).isEqualTo(3); // ceil(7/3) = 3
    }

    @Test
    @DisplayName("page와 pageSize가 결과에 그대로 반영된다")
    void getUserProductInquiryPosts_pageAndPageSizeReflected() {
        // given
        GetUserProductInquiryPostsCommand command = new GetUserProductInquiryPostsCommand(
                1L, 2, 5, Sort.Direction.ASC, PostSortProperty.ID
        );
        mockFindUserPostsByOffset(emptyPosts());
        when(findPostPort.countUserPosts(1L, Board.PRODUCT_INQUERY, null, null)).thenReturn(10L);

        // when
        GetUserProductInquiryPostsResult result = getUserProductInquiryPostsService.getUserProductInquiryPosts(command);

        // then
        assertThat(result.page()).isEqualTo(2);
        assertThat(result.pageSize()).isEqualTo(5);
        assertThat(result.totalPages()).isEqualTo(2); // ceil(10/5) = 2
    }

    // ========== C. Port 호출 검증 ==========

    @Test
    @DisplayName("findUserPostsByOffset이 PRODUCT_INQUERY 게시판으로 호출된다")
    void getUserProductInquiryPosts_callsFindUserPostsByOffsetWithProductInquiry() {
        // given
        Long userId = 1L;
        GetUserProductInquiryPostsCommand command = new GetUserProductInquiryPostsCommand(
                userId, 1, 10, Sort.Direction.DESC, PostSortProperty.ID
        );
        mockFindUserPostsByOffset(emptyPosts());
        when(findPostPort.countUserPosts(userId, Board.PRODUCT_INQUERY, null, null)).thenReturn(0L);

        // when
        getUserProductInquiryPostsService.getUserProductInquiryPosts(command);

        // then
        verify(findPostPort).findUserPostsByOffset(
                userId, Board.PRODUCT_INQUERY, 1, 10, true, PostSortProperty.ID
        );
    }

    @Test
    @DisplayName("countUserPosts가 PRODUCT_INQUERY 게시판으로 호출된다")
    void getUserProductInquiryPosts_callsCountUserPostsWithProductInquiry() {
        // given
        Long userId = 1L;
        GetUserProductInquiryPostsCommand command = new GetUserProductInquiryPostsCommand(
                userId, 1, 10, Sort.Direction.DESC, PostSortProperty.ID
        );
        mockFindUserPostsByOffset(emptyPosts());
        when(findPostPort.countUserPosts(userId, Board.PRODUCT_INQUERY, null, null)).thenReturn(0L);

        // when
        getUserProductInquiryPostsService.getUserProductInquiryPosts(command);

        // then
        verify(findPostPort).countUserPosts(userId, Board.PRODUCT_INQUERY, null, null);
    }

    // ========== D. 상품 정보 ==========

    @Test
    @DisplayName("targetId가 있는 게시글에 상품 정보가 포함된다")
    void getUserProductInquiryPosts_withTargetId_includesProductInfo() {
        // given
        Long userId = 1L;
        Long targetId = 100L;
        GetUserProductInquiryPostsCommand command = new GetUserProductInquiryPostsCommand(
                userId, 1, 10, Sort.Direction.DESC, PostSortProperty.ID
        );
        Posts posts = Posts.from(List.of(buildProductInquiryPost(1L, userId, targetId)));
        mockFindUserPostsByOffset(posts);
        when(findPostPort.countUserPosts(userId, Board.PRODUCT_INQUERY, null, null)).thenReturn(1L);
        ProductInfoResult productInfo = buildProductInfo(999L);
        when(findProductByPricePolicyPort.findByPricePolicyIds(List.of(targetId)))
                .thenReturn(Map.of(targetId, productInfo));

        // when
        GetUserProductInquiryPostsResult result = getUserProductInquiryPostsService.getUserProductInquiryPosts(command);

        // then
        assertThat(result.posts()).hasSize(1);
        assertThat(result.posts().getFirst().getProduct()).isNotNull();
        assertThat(result.posts().getFirst().getProduct().name()).isEqualTo("상품명");
    }

    @Test
    @DisplayName("targetId가 없는 게시글은 상품 정보 없이 반환된다")
    void getUserProductInquiryPosts_withoutTargetId_noProductInfo() {
        // given
        Long userId = 1L;
        GetUserProductInquiryPostsCommand command = new GetUserProductInquiryPostsCommand(
                userId, 1, 10, Sort.Direction.DESC, PostSortProperty.ID
        );
        Posts posts = Posts.from(List.of(buildProductInquiryPostWithoutTarget(1L, userId)));
        mockFindUserPostsByOffset(posts);
        when(findPostPort.countUserPosts(userId, Board.PRODUCT_INQUERY, null, null)).thenReturn(1L);
        mockProductInfoPort(Map.of());

        // when
        GetUserProductInquiryPostsResult result = getUserProductInquiryPostsService.getUserProductInquiryPosts(command);

        // then
        assertThat(result.posts()).hasSize(1);
        assertThat(result.posts().getFirst().getProduct()).isNull();
    }

    // ========== E. 이미지 ==========

    @Test
    @DisplayName("사진 게시글이면 이미지가 포함된다")
    void getUserProductInquiryPosts_photoPost_includesImages() {
        // given
        Long userId = 1L;
        GetUserProductInquiryPostsCommand command = new GetUserProductInquiryPostsCommand(
                userId, 1, 10, Sort.Direction.DESC, PostSortProperty.ID
        );
        Post photoPost = buildPhotoProductInquiryPost(1L, userId, 100L);
        Posts posts = Posts.from(List.of(photoPost));
        mockFindUserPostsByOffset(posts);
        when(findPostPort.countUserPosts(userId, Board.PRODUCT_INQUERY, null, null)).thenReturn(1L);
        mockProductInfoPort(Map.of());
        GetFileResult fileResult = new GetFileResult(
                1L, "POST_IMAGE", "jpg", "image.jpg", "https://s3/image.jpg", List.of(), 1L
        );
        when(findPostImagesPort.findImagesByPostIdAndSort(1L, POST_IMAGE))
                .thenReturn(Optional.of(new GetFilesResult(List.of(fileResult))));

        // when
        GetUserProductInquiryPostsResult result = getUserProductInquiryPostsService.getUserProductInquiryPosts(command);

        // then
        assertThat(result.posts()).hasSize(1);
        assertThat(result.posts().getFirst().getImages()).hasSize(1);
    }

    @Test
    @DisplayName("사진 게시글이 아니면 FindPostImagesPort가 호출되지 않는다")
    void getUserProductInquiryPosts_nonPhotoPost_doesNotCallImagePort() {
        // given
        Long userId = 1L;
        GetUserProductInquiryPostsCommand command = new GetUserProductInquiryPostsCommand(
                userId, 1, 10, Sort.Direction.DESC, PostSortProperty.ID
        );
        Posts posts = Posts.from(List.of(buildProductInquiryPost(1L, userId, 100L)));
        mockFindUserPostsByOffset(posts);
        when(findPostPort.countUserPosts(userId, Board.PRODUCT_INQUERY, null, null)).thenReturn(1L);
        mockProductInfoPort(Map.of());

        // when
        getUserProductInquiryPostsService.getUserProductInquiryPosts(command);

        // then
        verifyNoInteractions(findPostImagesPort);
    }

    // ========== F. 답글 ==========

    @Test
    @DisplayName("답글이 있는 게시글이면 답글이 UserProductInquiryPostItemResult에 포함된다")
    void getUserProductInquiryPosts_withReplies_repliesIncluded() {
        // given
        Long userId = 1L;
        Long targetId = 100L;
        GetUserProductInquiryPostsCommand command = new GetUserProductInquiryPostsCommand(
                userId, 1, 10, Sort.Direction.DESC, PostSortProperty.ID
        );
        Post parentPost = buildProductInquiryPost(1L, userId, targetId);
        Post replyPost = buildProductInquiryPost(2L, userId, targetId);
        parentPost.addReplies(List.of(replyPost));
        Posts posts = Posts.from(List.of(parentPost));
        mockFindUserPostsByOffset(posts);
        when(findPostPort.countUserPosts(userId, Board.PRODUCT_INQUERY, null, null)).thenReturn(1L);
        mockProductInfoPort(Map.of());

        // when
        GetUserProductInquiryPostsResult result = getUserProductInquiryPostsService.getUserProductInquiryPosts(command);

        // then
        assertThat(result.posts()).hasSize(1);
        assertThat(result.posts().getFirst().getReplies()).hasSize(1);
        assertThat(result.posts().getFirst().getReplies().getFirst().getId()).isEqualTo(2L);
    }

    // ========== G. 정렬 ==========

    @Test
    @DisplayName("ASC 정렬 시 isDesc가 false로 전달된다")
    void getUserProductInquiryPosts_ascDirection_isDescFalse() {
        // given
        Long userId = 1L;
        GetUserProductInquiryPostsCommand command = new GetUserProductInquiryPostsCommand(
                userId, 1, 10, Sort.Direction.ASC, PostSortProperty.ID
        );
        mockFindUserPostsByOffset(emptyPosts());
        when(findPostPort.countUserPosts(userId, Board.PRODUCT_INQUERY, null, null)).thenReturn(0L);

        // when
        getUserProductInquiryPostsService.getUserProductInquiryPosts(command);

        // then
        verify(findPostPort).findUserPostsByOffset(
                userId, Board.PRODUCT_INQUERY, 1, 10, false, PostSortProperty.ID
        );
    }

    // ========== Post Builders ==========

    private Post buildProductInquiryPost(Long id, Long userId, Long targetId) {
        return Post.from(PostSnapshotState.builder()
                .id(id)
                .userId(userId)
                .board(Board.PRODUCT_INQUERY)
                .category("PRODUCT_QUESTION")
                .targetType(PostTargetType.PRICE_POLICY)
                .targetId(targetId)
                .title("상품 문의 제목")
                .content("상품 문의 내용")
                .status(EntityStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .orderNum(id)
                .build());
    }

    private Post buildProductInquiryPostWithoutTarget(Long id, Long userId) {
        return Post.from(PostSnapshotState.builder()
                .id(id)
                .userId(userId)
                .board(Board.PRODUCT_INQUERY)
                .category("PRODUCT_QUESTION")
                .title("상품 문의 제목")
                .content("상품 문의 내용")
                .status(EntityStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .orderNum(id)
                .build());
    }

    private Post buildPhotoProductInquiryPost(Long id, Long userId, Long targetId) {
        return Post.from(PostSnapshotState.builder()
                .id(id)
                .userId(userId)
                .board(Board.PRODUCT_INQUERY)
                .category("PRODUCT_QUESTION")
                .targetType(PostTargetType.PRICE_POLICY)
                .targetId(targetId)
                .title("사진 상품 문의")
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

    private Posts emptyPosts() {
        return Posts.from(List.of());
    }

    private void mockFindUserPostsByOffset(Posts posts) {
        when(findPostPort.findUserPostsByOffset(
                anyLong(), any(Board.class), anyInt(), anyInt(), anyBoolean(), any(PostSortProperty.class)
        )).thenReturn(posts);
    }

    private void mockProductInfoPort(Map<Long, ProductInfoResult> infoMap) {
        when(findProductByPricePolicyPort.findByPricePolicyIds(anyList())).thenReturn(infoMap);
    }
}
