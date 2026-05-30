package com.personal.marketnote.community.service.post;

import com.personal.marketnote.common.application.file.port.in.result.GetFileResult;
import com.personal.marketnote.common.application.file.port.in.result.GetFilesResult;
import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.community.domain.post.*;
import com.personal.marketnote.community.port.in.command.post.GetUserOneOnOneInquiryPostsCommand;
import com.personal.marketnote.community.port.in.result.post.GetUserOneOnOneInquiryPostsResult;
import com.personal.marketnote.community.port.out.file.FindPostImagesPort;
import com.personal.marketnote.community.port.out.post.FindPostPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.personal.marketnote.common.domain.file.FileSort.POST_IMAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetUserOneOnOneInquiryPostsUseCaseTest {
    @Mock
    private FindPostPort findPostPort;
    @Mock
    private FindPostImagesPort findPostImagesPort;

    @InjectMocks
    private GetUserOneOnOneInquiryPostsService getUserOneOnOneInquiryPostsService;

    // ========== A. 기본 조회 ==========

    @Test
    @DisplayName("회원의 1:1 문의 내역을 오프셋 기반으로 조회한다")
    void getUserOneOnOneInquiryPosts_returnsOffsetBasedResult() {
        // given
        Long userId = 1L;
        GetUserOneOnOneInquiryPostsCommand command = new GetUserOneOnOneInquiryPostsCommand(
                userId, 1, 10, Sort.Direction.DESC, PostSortProperty.ID
        );
        Posts posts = Posts.from(List.of(
                buildOneOnOneInquiryPost(1L, userId),
                buildOneOnOneInquiryPost(2L, userId)
        ));
        mockFindUserPostsByOffset(posts);
        when(findPostPort.countUserPosts(userId, Board.ONE_ON_ONE_INQUERY, null, null, null, null)).thenReturn(2L);

        // when
        GetUserOneOnOneInquiryPostsResult result = getUserOneOnOneInquiryPostsService.getUserOneOnOneInquiryPosts(command);

        // then
        assertThat(result.page()).isEqualTo(1);
        assertThat(result.pageSize()).isEqualTo(10);
        assertThat(result.totalElements()).isEqualTo(2L);
        assertThat(result.totalPages()).isEqualTo(1);
        assertThat(result.posts()).hasSize(2);
    }

    @Test
    @DisplayName("조회 결과가 비어 있으면 빈 목록과 총 페이지 0을 반환한다")
    void getUserOneOnOneInquiryPosts_emptyResult_returnsEmptyListAndZeroPages() {
        // given
        GetUserOneOnOneInquiryPostsCommand command = new GetUserOneOnOneInquiryPostsCommand(
                1L, 1, 10, Sort.Direction.DESC, PostSortProperty.ID
        );
        mockFindUserPostsByOffset(emptyPosts());
        when(findPostPort.countUserPosts(1L, Board.ONE_ON_ONE_INQUERY, null, null, null, null)).thenReturn(0L);

        // when
        GetUserOneOnOneInquiryPostsResult result = getUserOneOnOneInquiryPostsService.getUserOneOnOneInquiryPosts(command);

        // then
        assertThat(result.posts()).isEmpty();
        assertThat(result.totalElements()).isZero();
        assertThat(result.totalPages()).isZero();
    }

    // ========== B. 페이지네이션 ==========

    @Test
    @DisplayName("totalPages가 올바르게 계산된다 (올림)")
    void getUserOneOnOneInquiryPosts_totalPagesComputedCorrectly() {
        // given
        GetUserOneOnOneInquiryPostsCommand command = new GetUserOneOnOneInquiryPostsCommand(
                1L, 1, 3, Sort.Direction.DESC, PostSortProperty.ID
        );
        Posts posts = Posts.from(List.of(
                buildOneOnOneInquiryPost(1L, 1L),
                buildOneOnOneInquiryPost(2L, 1L),
                buildOneOnOneInquiryPost(3L, 1L)
        ));
        mockFindUserPostsByOffset(posts);
        when(findPostPort.countUserPosts(1L, Board.ONE_ON_ONE_INQUERY, null, null, null, null)).thenReturn(7L);

        // when
        GetUserOneOnOneInquiryPostsResult result = getUserOneOnOneInquiryPostsService.getUserOneOnOneInquiryPosts(command);

        // then
        assertThat(result.totalPages()).isEqualTo(3); // ceil(7/3) = 3
    }

    @Test
    @DisplayName("page와 pageSize가 결과에 그대로 반영된다")
    void getUserOneOnOneInquiryPosts_pageAndPageSizeReflected() {
        // given
        GetUserOneOnOneInquiryPostsCommand command = new GetUserOneOnOneInquiryPostsCommand(
                1L, 2, 5, Sort.Direction.ASC, PostSortProperty.ID
        );
        mockFindUserPostsByOffset(emptyPosts());
        when(findPostPort.countUserPosts(1L, Board.ONE_ON_ONE_INQUERY, null, null, null, null)).thenReturn(10L);

        // when
        GetUserOneOnOneInquiryPostsResult result = getUserOneOnOneInquiryPostsService.getUserOneOnOneInquiryPosts(command);

        // then
        assertThat(result.page()).isEqualTo(2);
        assertThat(result.pageSize()).isEqualTo(5);
        assertThat(result.totalPages()).isEqualTo(2); // ceil(10/5) = 2
    }

    // ========== C. Port 호출 검증 ==========

    @Test
    @DisplayName("findUserPostsByOffset이 ONE_ON_ONE_INQUERY 게시판으로 호출된다")
    void getUserOneOnOneInquiryPosts_callsFindUserPostsByOffsetWithOneOnOneInquiry() {
        // given
        Long userId = 1L;
        GetUserOneOnOneInquiryPostsCommand command = new GetUserOneOnOneInquiryPostsCommand(
                userId, 1, 10, Sort.Direction.DESC, PostSortProperty.ID
        );
        mockFindUserPostsByOffset(emptyPosts());
        when(findPostPort.countUserPosts(userId, Board.ONE_ON_ONE_INQUERY, null, null, null, null)).thenReturn(0L);

        // when
        getUserOneOnOneInquiryPostsService.getUserOneOnOneInquiryPosts(command);

        // then
        verify(findPostPort).findUserPostsByOffset(
                userId, Board.ONE_ON_ONE_INQUERY, 1, 10, true, PostSortProperty.ID
        );
    }

    @Test
    @DisplayName("countUserPosts가 ONE_ON_ONE_INQUERY 게시판으로 호출된다")
    void getUserOneOnOneInquiryPosts_callsCountUserPostsWithOneOnOneInquiry() {
        // given
        Long userId = 1L;
        GetUserOneOnOneInquiryPostsCommand command = new GetUserOneOnOneInquiryPostsCommand(
                userId, 1, 10, Sort.Direction.DESC, PostSortProperty.ID
        );
        mockFindUserPostsByOffset(emptyPosts());
        when(findPostPort.countUserPosts(userId, Board.ONE_ON_ONE_INQUERY, null, null, null, null)).thenReturn(0L);

        // when
        getUserOneOnOneInquiryPostsService.getUserOneOnOneInquiryPosts(command);

        // then
        verify(findPostPort).countUserPosts(userId, Board.ONE_ON_ONE_INQUERY, null, null, null, null);
    }

    // ========== D. 이미지 ==========

    @Test
    @DisplayName("사진 게시글이면 이미지가 포함된다")
    void getUserOneOnOneInquiryPosts_photoPost_includesImages() {
        // given
        Long userId = 1L;
        GetUserOneOnOneInquiryPostsCommand command = new GetUserOneOnOneInquiryPostsCommand(
                userId, 1, 10, Sort.Direction.DESC, PostSortProperty.ID
        );
        Post photoPost = buildPhotoOneOnOneInquiryPost(1L, userId);
        Posts posts = Posts.from(List.of(photoPost));
        mockFindUserPostsByOffset(posts);
        when(findPostPort.countUserPosts(userId, Board.ONE_ON_ONE_INQUERY, null, null, null, null)).thenReturn(1L);
        GetFileResult fileResult = new GetFileResult(
                1L, "POST_IMAGE", "jpg", "image.jpg", "https://s3/image.jpg", List.of(), 1L
        );
        when(findPostImagesPort.findImagesByPostIdAndSort(1L, POST_IMAGE))
                .thenReturn(Optional.of(new GetFilesResult(List.of(fileResult))));

        // when
        GetUserOneOnOneInquiryPostsResult result = getUserOneOnOneInquiryPostsService.getUserOneOnOneInquiryPosts(command);

        // then
        assertThat(result.posts()).hasSize(1);
        assertThat(result.posts().getFirst().getImages()).hasSize(1);
    }

    @Test
    @DisplayName("사진 게시글이 아니면 FindPostImagesPort가 호출되지 않는다")
    void getUserOneOnOneInquiryPosts_nonPhotoPost_doesNotCallImagePort() {
        // given
        Long userId = 1L;
        GetUserOneOnOneInquiryPostsCommand command = new GetUserOneOnOneInquiryPostsCommand(
                userId, 1, 10, Sort.Direction.DESC, PostSortProperty.ID
        );
        Posts posts = Posts.from(List.of(buildOneOnOneInquiryPost(1L, userId)));
        mockFindUserPostsByOffset(posts);
        when(findPostPort.countUserPosts(userId, Board.ONE_ON_ONE_INQUERY, null, null, null, null)).thenReturn(1L);

        // when
        getUserOneOnOneInquiryPostsService.getUserOneOnOneInquiryPosts(command);

        // then
        verifyNoInteractions(findPostImagesPort);
    }

    // ========== E. 답글 ==========

    @Test
    @DisplayName("답글이 있는 게시글이면 답글이 PostItemResult에 포함된다")
    void getUserOneOnOneInquiryPosts_withReplies_repliesIncluded() {
        // given
        Long userId = 1L;
        GetUserOneOnOneInquiryPostsCommand command = new GetUserOneOnOneInquiryPostsCommand(
                userId, 1, 10, Sort.Direction.DESC, PostSortProperty.ID
        );
        Post parentPost = buildOneOnOneInquiryPost(1L, userId);
        Post replyPost = buildOneOnOneInquiryPost(2L, userId);
        parentPost.addReplies(List.of(replyPost));
        Posts posts = Posts.from(List.of(parentPost));
        mockFindUserPostsByOffset(posts);
        when(findPostPort.countUserPosts(userId, Board.ONE_ON_ONE_INQUERY, null, null, null, null)).thenReturn(1L);

        // when
        GetUserOneOnOneInquiryPostsResult result = getUserOneOnOneInquiryPostsService.getUserOneOnOneInquiryPosts(command);

        // then
        assertThat(result.posts()).hasSize(1);
        assertThat(result.posts().getFirst().getReplies()).hasSize(1);
        assertThat(result.posts().getFirst().getReplies().getFirst().getId()).isEqualTo(2L);
    }

    // ========== F. 정렬 ==========

    @Test
    @DisplayName("ASC 정렬 시 isDesc가 false로 전달된다")
    void getUserOneOnOneInquiryPosts_ascDirection_isDescFalse() {
        // given
        Long userId = 1L;
        GetUserOneOnOneInquiryPostsCommand command = new GetUserOneOnOneInquiryPostsCommand(
                userId, 1, 10, Sort.Direction.ASC, PostSortProperty.ID
        );
        mockFindUserPostsByOffset(emptyPosts());
        when(findPostPort.countUserPosts(userId, Board.ONE_ON_ONE_INQUERY, null, null, null, null)).thenReturn(0L);

        // when
        getUserOneOnOneInquiryPostsService.getUserOneOnOneInquiryPosts(command);

        // then
        verify(findPostPort).findUserPostsByOffset(
                userId, Board.ONE_ON_ONE_INQUERY, 1, 10, false, PostSortProperty.ID
        );
    }

    // ========== Post Builders ==========

    private Post buildOneOnOneInquiryPost(Long id, Long userId) {
        return Post.from(PostSnapshotState.builder()
                .id(id)
                .userId(userId)
                .board(Board.ONE_ON_ONE_INQUERY)
                .category("ORDER_PAYMENT")
                .title("1:1 문의 제목")
                .content("1:1 문의 내용")
                .status(EntityStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .orderNum(id)
                .build());
    }

    private Post buildPhotoOneOnOneInquiryPost(Long id, Long userId) {
        return Post.from(PostSnapshotState.builder()
                .id(id)
                .userId(userId)
                .board(Board.ONE_ON_ONE_INQUERY)
                .category("ORDER_PAYMENT")
                .title("사진 1:1 문의")
                .content("내용")
                .isPhoto(true)
                .status(EntityStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .orderNum(id)
                .build());
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
}
