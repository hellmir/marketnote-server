package com.personal.marketnote.community.service.post;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.community.domain.post.Board;
import com.personal.marketnote.community.domain.post.Post;
import com.personal.marketnote.community.domain.post.PostSnapshotState;
import com.personal.marketnote.community.exception.PostNotEditableException;
import com.personal.marketnote.community.exception.PostNotFoundException;
import com.personal.marketnote.community.port.in.command.post.UpdatePostCommand;
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

    private Post buildPost(Long id, Board board) {
        return Post.from(PostSnapshotState.builder()
                .id(id)
                .userId(1L)
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

    private String resolveCategoryCode(Board board) {
        return switch (board) {
            case NOTICE -> "ANNOUNCEMENT";
            case FAQ -> "ORDER_PAYMENT";
            case PRODUCT_INQUERY -> "PRODUCT_QUESTION";
            case ONE_ON_ONE_INQUERY -> "ORDER_PAYMENT";
        };
    }
}
