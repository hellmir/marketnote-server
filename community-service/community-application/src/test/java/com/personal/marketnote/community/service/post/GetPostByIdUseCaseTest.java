package com.personal.marketnote.community.service.post;

import com.personal.marketnote.common.adapter.out.persistence.audit.EntityStatus;
import com.personal.marketnote.community.domain.post.Board;
import com.personal.marketnote.community.domain.post.Post;
import com.personal.marketnote.community.domain.post.PostSnapshotState;
import com.personal.marketnote.community.exception.PostNotFoundException;
import com.personal.marketnote.community.port.out.post.FindPostPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetPostByIdUseCaseTest {
    @Mock
    private FindPostPort findPostPort;

    @InjectMocks
    private GetPostService getPostService;

    @Test
    @DisplayName("게시글 ID로 조회하면 게시글을 반환한다")
    void getPost_postExists_returnsPost() {
        Long postId = 1L;
        Post post = buildPost(postId);
        when(findPostPort.findById(postId)).thenReturn(Optional.of(post));

        Post result = getPostService.getPost(postId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(postId);
        assertThat(result.getTitle()).isEqualTo("문의 제목");
    }

    @Test
    @DisplayName("존재하지 않는 게시글 ID로 조회하면 PostNotFoundException을 발생시킨다")
    void getPost_postNotExists_throwsPostNotFoundException() {
        Long postId = 999L;
        when(findPostPort.findById(postId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> getPostService.getPost(postId))
                .isInstanceOf(PostNotFoundException.class)
                .hasMessageContaining(String.valueOf(postId));
    }

    private Post buildPost(Long id) {
        return Post.from(PostSnapshotState.builder()
                .id(id)
                .userId(1L)
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
}
