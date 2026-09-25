package com.personal.marketnote.community.service.post;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.community.domain.post.Board;
import com.personal.marketnote.community.domain.post.Post;
import com.personal.marketnote.community.domain.post.PostSnapshotState;
import com.personal.marketnote.community.exception.PostNotFoundException;
import com.personal.marketnote.community.port.in.result.post.GetPostKeyResult;
import com.personal.marketnote.community.port.out.post.FindPostPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetPostKeyUseCaseTest {
    @Mock
    private FindPostPort findPostPort;

    @InjectMocks
    private GetPostKeyService getPostKeyService;

    @Test
    @DisplayName("작성자 본인이 요청하면 postKey를 반환한다")
    void shouldReturnPostKeyWhenRequesterIsOwner() {
        Long postId = 1L;
        Long ownerId = 100L;
        UUID postKey = UUID.randomUUID();
        Post post = createPost(postId, ownerId, postKey);
        when(findPostPort.findById(postId)).thenReturn(Optional.of(post));

        GetPostKeyResult result = getPostKeyService.getPostKey(postId, ownerId);

        assertThat(result.postKey()).isEqualTo(postKey);
        verify(findPostPort).findById(postId);
        verifyNoMoreInteractions(findPostPort);
    }

    @Test
    @DisplayName("작성자가 아닌 사용자가 요청하면 PostNotFoundException이 발생한다")
    void shouldThrowPostNotFoundExceptionWhenRequesterIsNotOwner() {
        Long postId = 1L;
        Long ownerId = 100L;
        Long otherUserId = 200L;
        Post post = createPost(postId, ownerId, UUID.randomUUID());
        when(findPostPort.findById(postId)).thenReturn(Optional.of(post));

        assertThatThrownBy(() -> getPostKeyService.getPostKey(postId, otherUserId))
                .isInstanceOf(PostNotFoundException.class);
        verify(findPostPort).findById(postId);
        verifyNoMoreInteractions(findPostPort);
    }

    @Test
    @DisplayName("존재하지 않는 postId로 요청하면 PostNotFoundException이 발생한다")
    void shouldThrowPostNotFoundExceptionWhenPostDoesNotExist() {
        Long postId = 999L;
        Long userId = 100L;
        when(findPostPort.findById(postId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> getPostKeyService.getPostKey(postId, userId))
                .isInstanceOf(PostNotFoundException.class);
        verify(findPostPort).findById(postId);
        verifyNoMoreInteractions(findPostPort);
    }

    private Post createPost(Long postId, Long userId, UUID postKey) {
        return Post.from(PostSnapshotState.builder()
                .id(postId)
                .userId(userId)
                .postKey(postKey)
                .board(Board.ONE_ON_ONE_INQUERY)
                .category("ORDER_PAYMENT")
                .writerName("홍길동")
                .maskedWriterName("홍*동")
                .title("문의 제목")
                .content("문의 내용")
                .status(EntityStatus.ACTIVE)
                .build());
    }
}
