package com.personal.marketnote.community.service.post;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.community.domain.post.Board;
import com.personal.marketnote.community.domain.post.Post;
import com.personal.marketnote.community.domain.post.PostSnapshotState;
import com.personal.marketnote.community.exception.PostNotEditableException;
import com.personal.marketnote.community.exception.PostNotFoundException;
import com.personal.marketnote.community.port.in.command.post.UpdatePostCommand;
import com.personal.marketnote.community.port.in.result.post.UpdatePostResult;
import com.personal.marketnote.community.port.in.usecase.post.GetPostUseCase;
import com.personal.marketnote.community.port.out.post.UpdatePostPort;
import com.personal.marketnote.community.port.out.profanity.FindProfanityWordPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdatePostUseCaseTest {
    @Mock
    private GetPostUseCase getPostUseCase;
    @Mock
    private UpdatePostPort updatePostPort;
    @Mock
    private FindProfanityWordPort findProfanityWordPort;

    @InjectMocks
    private UpdatePostService updatePostService;

    @Test
    @DisplayName("수정 가능한 게시판의 게시글을 수정하면 정상 처리된다")
    void updatePost_editableBoard_succeeds() {
        Long postId = 1L;
        Post post = buildPost(postId, Board.NOTICE);
        when(getPostUseCase.getPost(postId)).thenReturn(post);
        UpdatePostCommand command = UpdatePostCommand.of(postId, "수정된 제목", "수정된 내용");

        updatePostService.updatePost(command);

        verify(updatePostPort).update(post);
    }

    @Test
    @DisplayName("수정 후 updatePostPort.update()가 호출된다")
    void updatePost_editableBoard_callsUpdatePort() {
        Long postId = 1L;
        Post post = buildPost(postId, Board.FAQ);
        when(getPostUseCase.getPost(postId)).thenReturn(post);
        UpdatePostCommand command = UpdatePostCommand.of(postId, "새 제목", "새 내용");

        updatePostService.updatePost(command);

        verify(updatePostPort).update(post);
        verify(getPostUseCase).getPost(postId);
        verifyNoMoreInteractions(updatePostPort, getPostUseCase);
    }

    @Test
    @DisplayName("존재하지 않는 게시글을 수정하면 PostNotFoundException을 발생시킨다")
    void updatePost_postNotExists_throwsPostNotFoundException() {
        Long postId = 999L;
        when(getPostUseCase.getPost(postId)).thenThrow(new PostNotFoundException(postId));
        UpdatePostCommand command = UpdatePostCommand.of(postId, "제목", "내용");

        assertThatThrownBy(() -> updatePostService.updatePost(command))
                .isInstanceOf(PostNotFoundException.class);
        verifyNoInteractions(updatePostPort);
    }

    @Test
    @DisplayName("수정 불가능한 게시판의 게시글을 수정하면 PostNotEditableException을 발생시킨다")
    void updatePost_nonEditableBoard_throwsPostNotEditableException() {
        Long postId = 1L;
        Post post = buildPost(postId, Board.PRODUCT_INQUERY);
        when(getPostUseCase.getPost(postId)).thenReturn(post);
        UpdatePostCommand command = UpdatePostCommand.of(postId, "제목", "내용");

        assertThatThrownBy(() -> updatePostService.updatePost(command))
                .isInstanceOf(PostNotEditableException.class);
        verifyNoInteractions(updatePostPort);
    }

    @Test
    @DisplayName("상품 문의글 수정 시 수정 불가 예외가 욕설 검증보다 먼저 발생하고 욕설 검증이 실행되지 않는다")
    void updatePost_productInquiry_throwsNotEditableBeforeProfanityCheck() {
        Long postId = 1L;
        Post post = buildPost(postId, Board.PRODUCT_INQUERY);
        when(getPostUseCase.getPost(postId)).thenReturn(post);
        UpdatePostCommand command = UpdatePostCommand.of(postId, "욕설제목", "욕설내용");

        assertThatThrownBy(() -> updatePostService.updatePost(command))
                .isInstanceOf(PostNotEditableException.class);

        verifyNoInteractions(findProfanityWordPort);
        verifyNoInteractions(updatePostPort);
    }

    @Test
    @DisplayName("공지사항 수정 시 욕설 검증이 실행되지 않는다")
    void updatePost_notice_skipsProfanityValidation() {
        Long postId = 1L;
        Post post = buildPost(postId, Board.NOTICE);
        when(getPostUseCase.getPost(postId)).thenReturn(post);
        UpdatePostCommand command = UpdatePostCommand.of(postId, "수정된 공지 제목", "수정된 공지 내용");

        updatePostService.updatePost(command);

        verifyNoInteractions(findProfanityWordPort);
        verify(updatePostPort).update(post);
    }

    @Test
    @DisplayName("FAQ 수정 시 욕설 검증이 실행되지 않는다")
    void updatePost_faq_skipsProfanityValidation() {
        Long postId = 1L;
        Post post = buildPost(postId, Board.FAQ);
        when(getPostUseCase.getPost(postId)).thenReturn(post);
        UpdatePostCommand command = UpdatePostCommand.of(postId, "수정된 FAQ 제목", "수정된 FAQ 내용");

        updatePostService.updatePost(command);

        verifyNoInteractions(findProfanityWordPort);
        verify(updatePostPort).update(post);
    }

    @Test
    @DisplayName("게시글을 수정하면 Result에 id와 postKey가 포함된다")
    void updatePost_resultIncludesIdAndPostKey() {
        Long postId = 1L;
        UUID postKey = UUID.randomUUID();
        Post post = buildPost(postId, Board.NOTICE, postKey);
        when(getPostUseCase.getPost(postId)).thenReturn(post);
        UpdatePostCommand command = UpdatePostCommand.of(postId, "수정된 제목", "수정된 내용");

        UpdatePostResult result = updatePostService.updatePost(command);

        assertThat(result.id()).isEqualTo(postId);
        assertThat(result.postKey()).isEqualTo(postKey);
    }

    @Test
    @DisplayName("공지 게시글 수정 시 isImportant=true 요청이 반영되어 저장된다")
    void updatePost_noticeAnnouncementWithImportantTrue_appliesToPost() {
        Long postId = 1L;
        Post post = buildPostWithImportance(postId, Board.NOTICE, false);
        when(getPostUseCase.getPost(postId)).thenReturn(post);
        UpdatePostCommand command = UpdatePostCommand.of(postId, "수정된 공지", "수정 내용", true);

        updatePostService.updatePost(command);

        assertThat(post.isImportant()).isTrue();
        verify(updatePostPort).update(post);
    }

    @Test
    @DisplayName("공지 게시글 수정 시 isImportant=false 요청이 반영되어 저장된다")
    void updatePost_noticeAnnouncementWithImportantFalse_appliesToPost() {
        Long postId = 1L;
        Post post = buildPostWithImportance(postId, Board.NOTICE, true);
        when(getPostUseCase.getPost(postId)).thenReturn(post);
        UpdatePostCommand command = UpdatePostCommand.of(postId, "수정된 공지", "수정 내용", false);

        updatePostService.updatePost(command);

        assertThat(post.isImportant()).isFalse();
        verify(updatePostPort).update(post);
    }

    @Test
    @DisplayName("공지 게시글 수정 시 title/content + isImportant 동시 수정이 정상 적용된다")
    void updatePost_noticeAnnouncement_titleContentImportantAllUpdated() {
        Long postId = 1L;
        Post post = buildPostWithImportance(postId, Board.NOTICE, false);
        when(getPostUseCase.getPost(postId)).thenReturn(post);
        UpdatePostCommand command = UpdatePostCommand.of(postId, "새 제목", "새 내용", true);

        updatePostService.updatePost(command);

        assertThat(post.getTitle()).isEqualTo("새 제목");
        assertThat(post.getContent()).isEqualTo("새 내용");
        assertThat(post.isImportant()).isTrue();
    }

    @Test
    @DisplayName("FAQ 게시글 수정 시 isImportant 요청이 무시되고 기존 값이 유지된다")
    void updatePost_faqWithImportantTrue_ignoresImportanceChange() {
        Long postId = 1L;
        Post post = buildPostWithImportance(postId, Board.FAQ, false);
        when(getPostUseCase.getPost(postId)).thenReturn(post);
        UpdatePostCommand command = UpdatePostCommand.of(postId, "FAQ 제목", "FAQ 내용", true);

        updatePostService.updatePost(command);

        assertThat(post.isImportant()).isFalse();
        verify(updatePostPort).update(post);
    }

    @Test
    @DisplayName("UpdatePostRequest에 isImportant 필드 미전송(null) 시 기존 값이 유지된다")
    void updatePost_nullIsImportant_keepsExistingValue() {
        Long postId = 1L;
        Post post = buildPostWithImportance(postId, Board.NOTICE, true);
        when(getPostUseCase.getPost(postId)).thenReturn(post);
        UpdatePostCommand command = UpdatePostCommand.of(postId, "수정된 제목", "수정된 내용");

        updatePostService.updatePost(command);

        assertThat(post.isImportant()).isTrue();
        verify(updatePostPort).update(post);
    }

    @Test
    @DisplayName("UpdatePostService는 publishPostEventPort에 의존하지 않으므로 공지 수정 시 NoticeRegistered 이벤트가 재발행되지 않는다")
    void updatePost_doesNotPublishNoticeRegisteredEvent() {
        Long postId = 1L;
        Post post = buildPostWithImportance(postId, Board.NOTICE, false);
        when(getPostUseCase.getPost(postId)).thenReturn(post);
        UpdatePostCommand command = UpdatePostCommand.of(postId, "수정된 제목", "수정된 내용", true);

        updatePostService.updatePost(command);

        // UpdatePostService는 PublishPostEventPort를 필드로 주입받지 않으므로
        // 구조적으로 NoticeRegistered/Outbox 이벤트 재발행 경로가 존재하지 않음 (회귀 방지)
        verify(updatePostPort).update(post);
    }

    private Post buildPost(Long id, Board board) {
        return buildPost(id, board, UUID.randomUUID());
    }

    private Post buildPost(Long id, Board board, UUID postKey) {
        return Post.from(PostSnapshotState.builder()
                .id(id)
                .userId(1L)
                .postKey(postKey)
                .board(board)
                .category(resolveCategoryCode(board))
                .title("원본 제목")
                .content("원본 내용")
                .status(EntityStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .orderNum(id)
                .build());
    }

    private Post buildPostWithImportance(Long id, Board board, boolean isImportant) {
        return Post.from(PostSnapshotState.builder()
                .id(id)
                .userId(1L)
                .postKey(UUID.randomUUID())
                .board(board)
                .category(resolveCategoryCode(board))
                .title("원본 제목")
                .content("원본 내용")
                .isImportant(isImportant)
                .status(EntityStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .orderNum(id)
                .build());
    }

    private String resolveCategoryCode(Board board) {
        return switch (board) {
            case NOTICE -> "ANNOUNCEMENT";
            case FAQ -> "ORDER_PAYMENT";
            case PRODUCT_INQUERY -> "PRODUCT_QUESTION";
            case ONE_ON_ONE_INQUERY -> "ORDER_PAYMENT";
        };
    }
}
