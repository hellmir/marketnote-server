package com.personal.marketnote.community.service.post;

import com.personal.marketnote.community.port.out.file.FindPostImagesPort;
import com.personal.marketnote.community.port.out.post.FindPostPort;
import com.personal.marketnote.community.port.out.product.FindProductByPricePolicyPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExistsPostUseCaseTest {
    @Mock
    private FindPostPort findPostPort;
    @Mock
    private FindProductByPricePolicyPort findProductByPricePolicyPort;
    @Mock
    private FindPostImagesPort findPostImagesPort;

    @InjectMocks
    private GetPostService getPostService;

    @Test
    @DisplayName("존재하는 게시글 ID로 조회하면 true를 반환한다")
    void existsPost_postExists_returnsTrue() {
        Long postId = 1L;
        when(findPostPort.existsById(postId)).thenReturn(true);

        boolean result = getPostService.existsPost(postId);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("존재하지 않는 게시글 ID로 조회하면 false를 반환한다")
    void existsPost_postNotExists_returnsFalse() {
        Long postId = 999L;
        when(findPostPort.existsById(postId)).thenReturn(false);

        boolean result = getPostService.existsPost(postId);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("전달받은 ID가 FindPostPort.existsById에 정확히 전달된다")
    void existsPost_passesIdToPort() {
        Long postId = 42L;
        when(findPostPort.existsById(postId)).thenReturn(true);

        getPostService.existsPost(postId);

        verify(findPostPort).existsById(postId);
    }

    @Test
    @DisplayName("FindPostPort.existsById가 정확히 1회 호출된다")
    void existsPost_callsPortExactlyOnce() {
        Long postId = 1L;
        when(findPostPort.existsById(postId)).thenReturn(true);

        getPostService.existsPost(postId);

        verify(findPostPort, times(1)).existsById(postId);
        verifyNoMoreInteractions(findPostPort);
    }

    @Test
    @DisplayName("existsPost 호출 시 FindProductByPricePolicyPort와 FindPostImagesPort는 호출되지 않는다")
    void existsPost_doesNotCallOtherPorts() {
        Long postId = 1L;
        when(findPostPort.existsById(postId)).thenReturn(true);

        getPostService.existsPost(postId);

        verifyNoInteractions(findProductByPricePolicyPort);
        verifyNoInteractions(findPostImagesPort);
    }

    @Test
    @DisplayName("null ID로 호출하면 FindPostPort.existsById에 null이 전달된다")
    void existsPost_nullId_delegatesToPort() {
        when(findPostPort.existsById(null)).thenReturn(false);

        boolean result = getPostService.existsPost(null);

        assertThat(result).isFalse();
        verify(findPostPort).existsById(null);
    }

    @Test
    @DisplayName("ID가 0이면 FindPostPort.existsById에 0이 전달되고 결과를 반환한다")
    void existsPost_zeroId_delegatesToPort() {
        Long postId = 0L;
        when(findPostPort.existsById(postId)).thenReturn(false);

        boolean result = getPostService.existsPost(postId);

        assertThat(result).isFalse();
        verify(findPostPort).existsById(postId);
    }

    @Test
    @DisplayName("음수 ID로 호출하면 FindPostPort.existsById에 음수 ID가 전달되고 결과를 반환한다")
    void existsPost_negativeId_delegatesToPort() {
        Long postId = -1L;
        when(findPostPort.existsById(postId)).thenReturn(false);

        boolean result = getPostService.existsPost(postId);

        assertThat(result).isFalse();
        verify(findPostPort).existsById(postId);
    }

    @Test
    @DisplayName("Long.MAX_VALUE ID로 호출하면 FindPostPort.existsById에 해당 값이 전달되고 결과를 반환한다")
    void existsPost_maxLongId_delegatesToPort() {
        Long postId = Long.MAX_VALUE;
        when(findPostPort.existsById(postId)).thenReturn(true);

        boolean result = getPostService.existsPost(postId);

        assertThat(result).isTrue();
        verify(findPostPort).existsById(postId);
    }

    @Test
    @DisplayName("FindPostPort.existsById 실행 중 예외가 발생하면 전파된다")
    void existsPost_portThrowsException_propagates() {
        Long postId = 1L;
        RuntimeException exception = new RuntimeException("database connection failed");
        when(findPostPort.existsById(postId)).thenThrow(exception);

        assertThatThrownBy(() -> getPostService.existsPost(postId))
                .isSameAs(exception);
    }

    @Test
    @DisplayName("서로 다른 ID로 각각 조회하면 각각에 해당하는 결과를 반환한다")
    void existsPost_differentIds_returnCorrespondingResults() {
        Long existingPostId = 1L;
        Long nonExistingPostId = 2L;
        when(findPostPort.existsById(existingPostId)).thenReturn(true);
        when(findPostPort.existsById(nonExistingPostId)).thenReturn(false);

        boolean existsResult = getPostService.existsPost(existingPostId);
        boolean notExistsResult = getPostService.existsPost(nonExistingPostId);

        assertThat(existsResult).isTrue();
        assertThat(notExistsResult).isFalse();
    }
}
